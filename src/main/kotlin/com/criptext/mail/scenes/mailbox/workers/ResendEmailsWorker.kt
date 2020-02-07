package com.criptext.mail.scenes.mailbox.workers

import com.criptext.mail.R
import com.criptext.mail.api.*
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.signal.RawSessionDao
import com.criptext.mail.db.models.*
import com.criptext.mail.scenes.composer.data.ComposerAPIClient
import com.criptext.mail.scenes.composer.data.PostEmailBody
import com.criptext.mail.scenes.mailbox.data.MailboxResult
import com.criptext.mail.scenes.mailbox.data.SentMailData
import com.criptext.mail.scenes.settings.profile.data.ProfileFooterData
import com.criptext.mail.signal.PreKeyBundleShareData
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalUtils
import com.criptext.mail.utils.*
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONObject
import org.whispersystems.libsignal.DuplicateMessageException
import java.io.File

class ResendEmailsWorker(
        private val signalClient: SignalClient,
        private val filesDir: File,
        private val rawSessionDao: RawSessionDao,
        private val db: MailboxLocalDB,
        private val activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private val accountDao: AccountDao,
        private val httpClient: HttpClient,
        override val publishFn: (
                MailboxResult.ResendEmails) -> Unit)
    : BackgroundWorker<MailboxResult.ResendEmails> {


    override val canBeParallelized = false

    private val fileHttpClient = HttpClient.Default(Hosts.fileServiceUrl, HttpClient.AuthScheme.jwt,
            14000L, 7000L)

    private val fileApiClient = ComposerAPIClient(fileHttpClient, activeAccount.jwt)
    private val apiClient = ComposerAPIClient(httpClient, activeAccount.jwt)

    private var meAsRecipient: Boolean = false
    private var currentFullEmail: FullEmail? = null
    private var guests: List<String> = listOf()
    private var isSecure = true

    private fun getDeliveryType(): DeliveryTypes{
        return if(meAsRecipient)
            DeliveryTypes.DELIVERED
        else
            DeliveryTypes.SENT
    }

    private fun checkEncryptionKeysOperation(mailRecipients: EmailUtils.MailRecipients)
            : Result<Unit, Exception> =
            Result.of { addMissingSessions(mailRecipients.criptextRecipients) }

    private fun encryptOperation(mailRecipients: EmailUtils.MailRecipients)
            : Result<List<PostEmailBody.CriptextEmail>, Exception> =
            Result.of { createEncryptedEmails(mailRecipients) }

    private fun getFileKey(fileKey: String?, attachments: List<CRFile>): String?{
        if(fileKey == null) return null
        val attachmentsThatNeedDuplicate = attachments.filter { db.fileNeedsDuplicate(it.id) }
        return if(attachmentsThatNeedDuplicate.isNotEmpty() && attachments.containsAll(attachmentsThatNeedDuplicate)) {
            attachmentsThatNeedDuplicate.first().fileKey
        }else{
            fileKey
        }
    }

    private fun getEncryptedFileKeys(fullEmail: FullEmail, recipientId: String, deviceId: Int): List<String>?{
        if(fullEmail.files.isEmpty()) return null
        val fileKeys = mutableListOf<String>()
        fullEmail.files.forEach {
            val fileKey = getFileKey(fullEmail.fileKey, fullEmail.files) ?: it.fileKey
            fileKeys.add(signalClient.encryptMessage(recipientId, deviceId, fileKey).encryptedB64)
        }
        return fileKeys
    }

    private fun getFileKeys(attachments: List<CRFile>): ArrayList<String>?{
        if(attachments.isEmpty()) return null
        val fileKeys: ArrayList<String> = ArrayList()
        attachments.mapTo(fileKeys) { it.fileKey }
        return fileKeys
    }

    private fun createCriptextAttachment(attachments: List<CRFile>)
            : List<PostEmailBody.CriptextAttachment> {
        val finalAttachments = mutableListOf<CRFile>()
        val attachmentsThatNeedDuplicate = attachments.filter { db.fileNeedsDuplicate(it.id) }
        if (attachmentsThatNeedDuplicate.isNotEmpty()) {
            finalAttachments.addAll(attachments.filter { it !in attachmentsThatNeedDuplicate })
            val op = Result.of { fileApiClient.duplicateAttachments(attachmentsThatNeedDuplicate.map { it.token }) }
            if(op is Result.Success){
                val httpReturn = JSONObject(op.value.body).getJSONObject("duplicates")
                for(file in attachmentsThatNeedDuplicate){
                    db.updateFileToken(file.id, httpReturn.getString(file.token))
                    finalAttachments.add(file)
                }
            }
        }else if(attachments.isNotEmpty()){
            finalAttachments.addAll(attachments)
        }
        return finalAttachments.map { attachment ->
            PostEmailBody.CriptextAttachment(token = attachment.token,
                    name = attachment.name, size = attachment.size, cid = attachment.cid)
        }
    }

    override fun catchException(ex: Exception): MailboxResult.ResendEmails
    {
        if(ex is ServerErrorException && ex.errorCode == ServerCodes.EnterpriseAccountSuspended)
            return MailboxResult.ResendEmails.EnterpriseSuspended()
        return MailboxResult.ResendEmails.Failure(createErrorMessage(ex))
    }


    override fun work(reporter: ProgressReporter<MailboxResult.ResendEmails>)
            : MailboxResult.ResendEmails? {
        val pendingEmails = db.getPendingEmails(listOf(DeliveryTypes.getTrueOrdinal(DeliveryTypes.FAIL)), activeAccount)
        if(pendingEmails.isEmpty()) return MailboxResult.ResendEmails.Failure(createErrorMessage(Exception()))
        for (email in pendingEmails) {
            meAsRecipient = setMeAsRecipient(email)
            currentFullEmail = email
            val operationResult = processSend()

            val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(operationResult)

            val finalResult = if(sessionExpired)
                newRetryWithNewSessionOperation()
            else
                operationResult

            if(finalResult is Result.Failure) return catchException(finalResult.error)
        }
        db.increaseContactScore(pendingEmails.map { it.email.id })
        return MailboxResult.ResendEmails.Success()
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private fun setMeAsRecipient(fullEmail: FullEmail): Boolean{
        return fullEmail.bcc.map { it.email }.contains(activeAccount.userEmail)
                || fullEmail.cc.map { it.email }.contains(activeAccount.userEmail)
                || fullEmail.to.map { it.email }.contains(activeAccount.userEmail)
    }

    private fun processSend(): Result<Unit, Exception>{
        return if(currentFullEmail != null) {
            val mailRecipients = EmailUtils.getMailRecipients(currentFullEmail!!.to,
                    currentFullEmail!!.cc, currentFullEmail!!.bcc, activeAccount.recipientId,
                    activeAccount.domain)
            checkEncryptionKeysOperation(mailRecipients)
                    .flatMap { encryptOperation(mailRecipients) }
                    .flatMap(sendEmailOperation)
                    .flatMap(updateSentMailInDB)
        }else
            Result.error(NullPointerException())
    }

    private fun newRetryWithNewSessionOperation()
            : Result<Unit, Exception> {
        val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient,
                activeAccount, storage, accountDao)
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when(refreshOperation){
            is Result.Success -> {
                fileApiClient.token = refreshOperation.value
                apiClient.token = refreshOperation.value
                processSend()
            }
            is Result.Failure -> {
                Result.of { throw refreshOperation.error }
            }
        }
    }

    private fun addMissingSessions(criptextRecipients: List<String>) {
        val knownAddresses = findKnownAddresses(criptextRecipients)

        val findKeyBundlesResponse = apiClient.findKeyBundles(criptextRecipients, knownAddresses)
        val bundlesJSONArray = JSONObject(findKeyBundlesResponse.body).getJSONArray("keyBundles")
        val blackListedJSONArray = JSONObject(findKeyBundlesResponse.body).getJSONArray("blacklistedKnownDevices")
        guests = JSONObject(findKeyBundlesResponse.body).getJSONArray("guestDomains").toList()
        if (bundlesJSONArray.length() > 0) {
            val downloadedBundles =
                    PreKeyBundleShareData.DownloadBundle.fromJSONArray(bundlesJSONArray, activeAccount.id)
            signalClient.createSessionsFromBundles(downloadedBundles.filter { it.shareData.domain !in guests })
        }
        if (blackListedJSONArray.length() > 0) {
            for (i in 0 until blackListedJSONArray.length())
            {
                val addresses = SignalUtils.getSignalAddressFromJSON(blackListedJSONArray[i].toString())
                signalClient.deleteSessions(addresses)
            }
        }
    }

    private fun findKnownAddresses(criptextRecipients: List<String>): Map<String, List<Int>> {
        val knownAddresses = HashMap<String, List<Int>>()
        val existingSessions = (rawSessionDao.getKnownAddresses(criptextRecipients.map {
            if (EmailAddressUtils.isFromCriptextDomain(it))
                EmailAddressUtils.extractRecipientIdFromCriptextAddress(it)
            else
                it
        }, activeAccount.id))
        existingSessions.forEach { knownAddress: KnownAddress ->
            if(!knownAddress.recipientId.contains("@"))
                knownAddress.recipientId = knownAddress.recipientId.plus(EmailAddressUtils.CRIPTEXT_DOMAIN_SUFFIX)
            knownAddresses[knownAddress.recipientId] = knownAddresses[knownAddress.recipientId]
                    ?.plus(knownAddress.deviceId)
                    ?: listOf(knownAddress.deviceId)
        }
        return knownAddresses
    }

    private fun createEncryptedEmails(mailRecipients: EmailUtils.MailRecipients): List<PostEmailBody.CriptextEmail> {
        val knownCriptextAddresses = findKnownAddresses(mailRecipients.criptextRecipients)
        val criptextToEmails = encryptForCriptextRecipients(currentFullEmail!!,
                mailRecipients.toCriptext, knownCriptextAddresses, PostEmailBody.RecipientTypes.to)
        val criptextCcEmails = encryptForCriptextRecipients(currentFullEmail!!,
                mailRecipients.ccCriptext, knownCriptextAddresses, PostEmailBody.RecipientTypes.cc)
        val criptextBccEmails = encryptForCriptextRecipients(currentFullEmail!!,
                mailRecipients.bccCriptext, knownCriptextAddresses, PostEmailBody.RecipientTypes.bcc)
        val criptextPeerEmails = encryptForCriptextRecipients(currentFullEmail!!,
                mailRecipients.peerCriptext, knownCriptextAddresses, PostEmailBody.RecipientTypes.peer)
        return listOf(criptextToEmails, criptextCcEmails, criptextBccEmails, criptextPeerEmails).flatten()
    }

    private fun encryptForCriptextRecipients(fullEmail: FullEmail, criptextRecipients: List<String>,
                                             availableAddresses: Map<String, List<Int>>,
                                             type: PostEmailBody.RecipientTypes)
            : List<PostEmailBody.CriptextEmail> {
        return criptextRecipients
                .filter { EmailAddressUtils.extractEmailAddressDomain(it) !in guests }
                .map { emailAddress ->
            val domain: String
            val recipientId = if(EmailAddressUtils.isFromCriptextDomain(emailAddress)) {
                domain = Contact.mainDomain
                EmailAddressUtils.extractRecipientIdFromCriptextAddress(emailAddress)
            }else {
                domain = EmailAddressUtils.extractEmailAddressDomain(emailAddress)
                emailAddress
            }
            val devices = availableAddresses[emailAddress]
            if (devices == null || devices.isEmpty()) {
                if (type == PostEmailBody.RecipientTypes.peer)
                    return emptyList()
                return if(domain == Contact.mainDomain) listOf(PostEmailBody.EmptyCriptextEmail(recipientId, domain))
                else listOf(PostEmailBody.EmptyCriptextEmail(EmailAddressUtils.extractRecipientIdFromAddress(recipientId, domain), domain))
            }
            devices.filter { deviceId ->
                type != PostEmailBody.RecipientTypes.peer || deviceId != activeAccount.deviceId
            }.map { deviceId ->
                val encryptOperation = Result.of {
                    val encryptedData = signalClient.encryptMessage(recipientId, deviceId, fullEmail.email.content)
                    val encryptedPreview = signalClient.encryptMessage(recipientId, deviceId, fullEmail.email.preview)
                    Pair(encryptedData, encryptedPreview)
                }
                when(encryptOperation){
                    is Result.Success -> {
                        PostEmailBody.CompleteCriptextEmail(recipientId = if(domain != Contact.mainDomain)
                            EmailAddressUtils.extractRecipientIdFromAddress(recipientId, domain)
                        else
                            recipientId, deviceId = deviceId,
                                type = type, body = encryptOperation.value.first.encryptedB64,
                                messageType = encryptOperation.value.first.type, fileKey = if(getFileKey(fullEmail.fileKey, fullEmail.files) != null)
                            signalClient.encryptMessage(recipientId, deviceId, getFileKey(fullEmail.fileKey, fullEmail.files)!!).encryptedB64
                        else null, fileKeys = getEncryptedFileKeys(fullEmail, recipientId, deviceId),
                                preview = encryptOperation.value.second.encryptedB64, previewMessageType = encryptOperation.value.second.type,
                                domain = domain)
                    }
                    is Result.Failure -> {
                        if(domain == Contact.mainDomain) PostEmailBody.EmptyCriptextEmail(recipientId, domain)
                        else PostEmailBody.EmptyCriptextEmail(EmailAddressUtils.extractRecipientIdFromAddress(recipientId, domain), domain)
                    }
                }
            }
        }.flatten()
    }

    private val sendEmailOperation
            : (List<PostEmailBody.CriptextEmail>) -> Result<String, Exception> =
            { criptextEmails ->
                Result.of {
                    val requestBody = PostEmailBody(
                            threadId = EmailUtils.getThreadIdForSending(currentFullEmail!!.email),
                            subject = currentFullEmail!!.email.subject,
                            criptextEmails = criptextEmails,
                            guestEmail = getGuestEmails(currentFullEmail!!,
                                    EmailUtils.getMailRecipientsNonCriptext(
                                            currentFullEmail!!.to.filter { EmailAddressUtils.extractEmailAddressDomain(it.email) in guests },
                                            currentFullEmail!!.cc.filter { EmailAddressUtils.extractEmailAddressDomain(it.email) in guests },
                                            currentFullEmail!!.bcc.filter { EmailAddressUtils.extractEmailAddressDomain(it.email) in guests },
                                            activeAccount.recipientId
                                    )),
                            attachments = createCriptextAttachment(currentFullEmail!!.files))
                    apiClient.postEmail(requestBody).body
                }.mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
            }

    private val updateSentMailInDB: (String) -> Result<Unit, Exception> =
            { response ->
                Result.of {
                    val email = currentFullEmail!!.email
                    val emailContent = EmailUtils.getEmailContentFromFileSystem(
                            filesDir = filesDir,
                            metadataKey = email.metadataKey,
                            recipientId = activeAccount.recipientId,
                            dbContent = email.content,
                            domain = activeAccount.domain
                    )
                    val sentMailData = SentMailData.fromJSON(JSONObject(response))
                    db.updateEmailAndAddLabel(id = currentFullEmail!!.email.id, threadId = sentMailData.threadId,
                            messageId = sentMailData.messageId, metadataKey = sentMailData.metadataKey,
                            status = getDeliveryType(),
                            date = DateAndTimeUtils.getDateFromString(sentMailData.date, null),
                            accountId = activeAccount.id, isSecure = isSecure
                    )

                    EmailUtils.saveEmailInFileSystem(filesDir = filesDir, content = emailContent.first,
                            recipientId = activeAccount.recipientId, metadataKey = sentMailData.metadataKey,
                            headers = emailContent.second, domain = activeAccount.domain)

                    EmailUtils.deleteEmailInFileSystem(filesDir = filesDir,
                            metadataKey = email.metadataKey, recipientId = activeAccount.recipientId,
                            domain = activeAccount.domain)
                }
            }

    private fun getGuestEmails(fullEmail: FullEmail, mailRecipientsNonCriptext: EmailUtils.MailRecipients) : PostEmailBody.GuestEmail?{
        val externalData = db.getExternalData(fullEmail.email.id)
        return if(externalData == null) {
            isSecure = false
            val showFooter = when {
                activeAccount.domain != Contact.mainDomain -> false
                storage.getString(KeyValueStorage.StringKey.ShowCriptextFooter, "").isNotEmpty() -> {
                    val footerData = ProfileFooterData.fromJson(storage.getString(KeyValueStorage.StringKey.ShowCriptextFooter, ""))
                    footerData.find { it.accountId == activeAccount.id }?.hasFooterEnabled ?: true
                }
                else -> true
            }
            PostEmailBody.GuestEmail(mailRecipientsNonCriptext.toCriptext,
                    mailRecipientsNonCriptext.ccCriptext, mailRecipientsNonCriptext.bccCriptext,
                    if(!showFooter) fullEmail.email.content
                    else HTMLUtils.addCriptextFooter(fullEmail.email.content), null, null, null,
                    fullEmail.fileKey, fileKeys = getFileKeys(fullEmail.files))
        }else {
            PostEmailBody.GuestEmail(mailRecipientsNonCriptext.toCriptext,
                    mailRecipientsNonCriptext.ccCriptext, mailRecipientsNonCriptext.bccCriptext,
                    externalData.encryptedBody, externalData.salt, externalData.iv,
                    externalData.encryptedSession, null, fileKeys = getFileKeys(fullEmail.files))
        }
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when(ex) {
            is ServerErrorException -> UIMessage(resId = R.string.send_bad_status, args = arrayOf(ex.errorCode))
            else -> {
                UIMessage(resId = R.string.send_try_again_error)
            }
        }
    }
}
