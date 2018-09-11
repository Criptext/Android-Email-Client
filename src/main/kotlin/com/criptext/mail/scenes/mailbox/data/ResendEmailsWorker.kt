package com.criptext.mail.scenes.mailbox.data

import com.criptext.mail.R
import com.criptext.mail.aes.AESUtil
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.dao.signal.RawIdentityKeyDao
import com.criptext.mail.db.dao.signal.RawSessionDao
import com.criptext.mail.db.models.*
import com.criptext.mail.scenes.composer.data.ComposerAPIClient
import com.criptext.mail.scenes.composer.data.ComposerAttachment
import com.criptext.mail.scenes.composer.data.PostEmailBody
import com.criptext.mail.signal.PreKeyBundleShareData
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.utils.*
import com.criptext.mail.utils.file.FileUtils
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.failure
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONArray
import org.json.JSONObject
import org.whispersystems.libsignal.DuplicateMessageException

class ResendEmailsWorker(
        private val signalClient: SignalClient,
        private val rawSessionDao: RawSessionDao,
        private val db: MailboxLocalDB,
        private val activeAccount: ActiveAccount,
        httpClient: HttpClient,
        override val publishFn: (
                MailboxResult.ResendEmails) -> Unit)
    : BackgroundWorker<MailboxResult.ResendEmails> {


    override val canBeParallelized = false
    private val apiClient = ComposerAPIClient(httpClient, activeAccount.jwt)

    private var meAsRecipient: Boolean = false
    private var currentFullEmail: FullEmail? = null

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

    private fun createCriptextAttachment(attachments: List<CRFile>)
            : List<PostEmailBody.CriptextAttachment> = attachments.map { attachment ->
        PostEmailBody.CriptextAttachment(token = attachment.token,
                name = attachment.name, size = attachment.size)
    }

    override fun catchException(ex: Exception): MailboxResult.ResendEmails =
       MailboxResult.ResendEmails.Failure()

    override fun work(reporter: ProgressReporter<MailboxResult.ResendEmails>)
            : MailboxResult.ResendEmails? {
        val pendingEmails = db.getPendingEmails(listOf(DeliveryTypes.getTrueOrdinal(DeliveryTypes.FAIL),
                DeliveryTypes.getTrueOrdinal(DeliveryTypes.SENDING)))
        if(pendingEmails.isEmpty()) return MailboxResult.ResendEmails.Failure()
        for (email in pendingEmails) {
            meAsRecipient = setMeAsRecipient(email)
            currentFullEmail = email
            val operationResult = processSend()
            if(operationResult is Result.Failure) return catchException(operationResult.error)
        }
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
                    currentFullEmail!!.cc, currentFullEmail!!.bcc, activeAccount.recipientId)
            checkEncryptionKeysOperation(mailRecipients)
                    .flatMap { encryptOperation(mailRecipients) }
                    .flatMap(sendEmailOperation)
                    .flatMap(updateSentMailInDB)
        }else
            Result.error(NullPointerException())
    }

    private fun addMissingSessions(criptextRecipients: List<String>) {
        val knownAddresses = findKnownAddresses(criptextRecipients)

        val findKeyBundlesResponse = apiClient.findKeyBundles(criptextRecipients, knownAddresses)
        val bundlesJSONArray = JSONArray(findKeyBundlesResponse)
        if (bundlesJSONArray.length() > 0) {
            val downloadedBundles =
                    PreKeyBundleShareData.DownloadBundle.fromJSONArray(bundlesJSONArray)
            signalClient.createSessionsFromBundles(downloadedBundles)
        }
    }

    private fun findKnownAddresses(criptextRecipients: List<String>): Map<String, List<Int>> {
        val knownAddresses = HashMap<String, List<Int>>()
        val existingSessions = rawSessionDao.getKnownAddresses(criptextRecipients)
        existingSessions.forEach { knownAddress: KnownAddress ->
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
        return criptextRecipients.map { recipientId ->
            val devices = availableAddresses[recipientId]
            if (devices == null || devices.isEmpty()) {
                if (type == PostEmailBody.RecipientTypes.peer)
                    return emptyList()
                throw IllegalArgumentException("Signal address for '$recipientId' does not exist in the store")
            }
            devices.filter { deviceId ->
                type != PostEmailBody.RecipientTypes.peer || deviceId != activeAccount.deviceId
            }.map { deviceId ->
                val encryptedData = signalClient.encryptMessage(recipientId, deviceId, fullEmail.email.content)
                PostEmailBody.CriptextEmail(recipientId = recipientId, deviceId = deviceId,
                        type = type, body = encryptedData.encryptedB64,
                        messageType = encryptedData.type, fileKey = if(fullEmail.fileKey != null)
                    signalClient.encryptMessage(recipientId, deviceId, fullEmail.fileKey).encryptedB64
                else null)
            }
        }.flatten()
    }

    private val sendEmailOperation
            : (List<PostEmailBody.CriptextEmail>) -> Result<String, Exception> =
            { criptextEmails ->
                Result.of {
                    val requestBody = PostEmailBody(
                            threadId = currentFullEmail!!.email.threadId,
                            subject = currentFullEmail!!.email.subject,
                            criptextEmails = criptextEmails,
                            guestEmail = getGuestEmails(currentFullEmail!!,
                                    EmailUtils.getMailRecipientsNonCriptext(
                                            currentFullEmail!!.to,
                                            currentFullEmail!!.cc,
                                            currentFullEmail!!.bcc,
                                            activeAccount.recipientId
                                    )),
                            attachments = createCriptextAttachment(currentFullEmail!!.files))
                    apiClient.postEmail(requestBody)
                }.mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
            }

    private val updateSentMailInDB: (String) -> Result<Unit, Exception> =
            { response ->
                Result.of {
                    val sentMailData = SentMailData.fromJSON(JSONObject(response))
                    db.updateEmailAndAddLabel(id = currentFullEmail!!.email.id, threadId = sentMailData.threadId,
                            messageId = sentMailData.messageId, metadataKey = sentMailData.metadataKey,
                            status = getDeliveryType(),
                            date = DateUtils.getDateFromString(sentMailData.date, null)
                    )
                }
            }

    private fun getGuestEmails(fullEmail: FullEmail, mailRecipientsNonCriptext: EmailUtils.MailRecipients) : PostEmailBody.GuestEmail?{
        val externalData = db.getExternalData(fullEmail.email.id)
        return if(externalData == null) {
            PostEmailBody.GuestEmail(mailRecipientsNonCriptext.toCriptext,
                    mailRecipientsNonCriptext.ccCriptext, mailRecipientsNonCriptext.bccCriptext,
                    getAttachmentsForUnencryptedGuestEmails(fullEmail), null, null, null)
        }else {
            PostEmailBody.GuestEmail(mailRecipientsNonCriptext.toCriptext,
                    mailRecipientsNonCriptext.ccCriptext, mailRecipientsNonCriptext.bccCriptext,
                    externalData.encryptedBody, externalData.salt, externalData.iv, externalData.encryptedSession)
        }
    }

    private fun getAttachmentsForUnencryptedGuestEmails(fullEmail: FullEmail): String{

        val bodyWithAttachments = StringBuilder()
        bodyWithAttachments.append(fullEmail.email.content)

        for (attachment in fullEmail.files){
            val mimeTypeSource = HTMLUtils.getMimeTypeSourceForUnencryptedEmail(
                    FileUtils.getMimeType(attachment.name))
            val encodedParams = Encoding.byteArrayToString((attachment.token+":"+fullEmail.fileKey)
                    .toByteArray())
            bodyWithAttachments.append(HTMLUtils.createAttchmentForUnencryptedEmailToNonCriptextUsers(
                    attachmentName = attachment.name, attachmentSize = attachment.size,
                    encodedParams = encodedParams, mimeTypeSource = mimeTypeSource)
            )
        }
        return bodyWithAttachments.toString()
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when(ex) {
            is ServerErrorException -> UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
            is DuplicateMessageException ->
                UIMessage(resId = R.string.email_already_decrypted)
            else -> {
                UIMessage(resId = R.string.failed_getting_emails)
            }
        }
    }
}
