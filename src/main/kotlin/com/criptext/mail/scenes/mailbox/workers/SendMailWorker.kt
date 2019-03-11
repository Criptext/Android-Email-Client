package com.criptext.mail.scenes.mailbox.workers

import android.accounts.NetworkErrorException
import com.criptext.mail.R
import com.criptext.mail.aes.AESUtil
import com.criptext.mail.api.Hosts
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.signal.RawIdentityKeyDao
import com.criptext.mail.db.dao.signal.RawSessionDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.EmailExternalSession
import com.criptext.mail.db.models.KnownAddress
import com.criptext.mail.scenes.composer.data.*
import com.criptext.mail.scenes.mailbox.data.MailboxResult
import com.criptext.mail.scenes.mailbox.data.SentMailData
import com.criptext.mail.signal.*
import com.criptext.mail.utils.*
import com.criptext.mail.utils.file.FileUtils
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File


/**
 * Created by gabriel on 2/26/18.
 */

class SendMailWorker(private val signalClient: SignalClient,
                     private val rawSessionDao: RawSessionDao,
                     private val rawIdentityKeyDao: RawIdentityKeyDao,
                     private val db: MailboxLocalDB,
                     private val filesDir: File,
                     private val httpClient: HttpClient,
                     private val activeAccount: ActiveAccount,
                     private val emailId: Long,
                     private val threadId: String?,
                     private val composerInputData: ComposerInputData,
                     private val attachments: List<ComposerAttachment>,
                     private val fileKey: String?,
                     private val storage: KeyValueStorage,
                     private val accountDao: AccountDao,
                     override val publishFn: (MailboxResult.SendMail) -> Unit)
    : BackgroundWorker<MailboxResult.SendMail> {
    override val canBeParallelized = false

    private val fileHttpClient = HttpClient.Default(Hosts.fileServiceUrl, HttpClient.AuthScheme.jwt,
            14000L, 7000L)

    private val fileApiClient = ComposerAPIClient(fileHttpClient, activeAccount.jwt)
    private val apiClient = ComposerAPIClient(httpClient, activeAccount.jwt)

    private var guestEmails: PostEmailBody.GuestEmail? = null

    private val meAsRecipient = composerInputData.bcc.map { it.email }.contains(activeAccount.userEmail)
            || composerInputData.cc.map { it.email }.contains(activeAccount.userEmail)
            || composerInputData.to.map { it.email }.contains(activeAccount.userEmail)

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

    private fun addMissingSessions(criptextRecipients: List<String>) {
        val knownAddresses = findKnownAddresses(criptextRecipients)

        val findKeyBundlesResponse = apiClient.findKeyBundles(criptextRecipients, knownAddresses)
        val bundlesJSONArray = JSONObject(findKeyBundlesResponse.body).getJSONArray("keyBundles")
        val blackListedJSONArray = JSONObject(findKeyBundlesResponse.body).getJSONArray("blacklistedKnownDevices")
        if (bundlesJSONArray.length() > 0) {
            val downloadedBundles =
                    PreKeyBundleShareData.DownloadBundle.fromJSONArray(bundlesJSONArray)
            signalClient.createSessionsFromBundles(downloadedBundles)
        }
        if (blackListedJSONArray.length() > 0) {
            for (i in 0 until blackListedJSONArray.length())
            {
                val addresses = SignalUtils.getSignalAddressFromJSON(blackListedJSONArray[i].toString())
                signalClient.deleteSessions(addresses)
            }
        }
    }

    private fun getDeliveryType(): DeliveryTypes{
        return if(meAsRecipient)
            DeliveryTypes.DELIVERED
        else
            DeliveryTypes.SENT
    }

    private fun encryptForCriptextRecipients(criptextRecipients: List<String>,
                                             availableAddresses: Map<String, List<Int>>,
                                             type: PostEmailBody.RecipientTypes)
            : List<PostEmailBody.CriptextEmail> {
        return criptextRecipients.map { recipientId ->
            val devices = availableAddresses[recipientId]
            if (devices == null || devices.isEmpty()) {
                if (type == PostEmailBody.RecipientTypes.peer)
                    return emptyList()
                return listOf(PostEmailBody.EmptyCriptextEmail(recipientId))
            }
            devices.filter { deviceId ->
                type != PostEmailBody.RecipientTypes.peer || deviceId != activeAccount.deviceId
            }.map { deviceId ->
                val encryptOperation = Result.of {
                    val encryptedData = signalClient.encryptMessage(recipientId, deviceId, composerInputData.body)
                    val email = db.getEmailById(emailId)!!
                    val encryptedPreview = signalClient.encryptMessage(recipientId, deviceId, email.preview)
                    Pair(encryptedData, encryptedPreview)
                }
                when(encryptOperation){
                    is Result.Success -> {
                        PostEmailBody.CompleteCriptextEmail(recipientId = recipientId, deviceId = deviceId,
                                type = type, body = encryptOperation.value.first.encryptedB64,
                                messageType = encryptOperation.value.first.type, fileKey = if(getFileKey() != null)
                            signalClient.encryptMessage(recipientId, deviceId, getFileKey()!!).encryptedB64
                        else null, fileKeys = getEncryptedFileKeys(recipientId, deviceId),
                                preview = encryptOperation.value.second.encryptedB64,
                                previewMessageType = encryptOperation.value.second.type)
                    }
                    is Result.Failure -> {
                        PostEmailBody.EmptyCriptextEmail(recipientId)
                    }
                }


            }
        }.flatten()
    }

    private fun createEncryptedEmails(mailRecipients: EmailUtils.MailRecipients): List<PostEmailBody.CriptextEmail> {
        val knownCriptextAddresses = findKnownAddresses(mailRecipients.criptextRecipients)
        val criptextToEmails = encryptForCriptextRecipients(mailRecipients.toCriptext,
                knownCriptextAddresses, PostEmailBody.RecipientTypes.to)
        val criptextCcEmails = encryptForCriptextRecipients(mailRecipients.ccCriptext,
                knownCriptextAddresses, PostEmailBody.RecipientTypes.cc)
        val criptextBccEmails = encryptForCriptextRecipients(mailRecipients.bccCriptext,
                knownCriptextAddresses, PostEmailBody.RecipientTypes.bcc)
        val criptextPeerEmails = encryptForCriptextRecipients(mailRecipients.peerCriptext,
                knownCriptextAddresses, PostEmailBody.RecipientTypes.peer)
        return listOf(criptextToEmails, criptextCcEmails, criptextBccEmails, criptextPeerEmails).flatten()
    }

    override fun catchException(ex: Exception): MailboxResult.SendMail {
        return if(ex is ServerErrorException) {
            when {
                ex.errorCode == ServerCodes.Unauthorized ->
                    MailboxResult.SendMail.Unauthorized(UIMessage(R.string.device_removed_remotely_exception))
                ex.errorCode == ServerCodes.Forbidden ->
                    MailboxResult.SendMail.Forbidden()
                ex.errorCode == ServerCodes.TooManyRequests ->
                    MailboxResult.SendMail.Failure(UIMessage(R.string.send_limit_reached))

                else -> MailboxResult.SendMail.Failure(createErrorMessage(ex))
            }
        }else {
            val message = createErrorMessage(ex)
            MailboxResult.SendMail.Failure(message)
        }
    }

    private fun checkEncryptionKeysOperation(mailRecipients: EmailUtils.MailRecipients)
            : Result<Unit, Exception> =
            Result.of { addMissingSessions(mailRecipients.criptextRecipients) }

    private fun encryptOperation(mailRecipients: EmailUtils.MailRecipients)
            : Result<List<PostEmailBody.CriptextEmail>, Exception> =
            Result.of { createEncryptedEmails(mailRecipients) }

    private fun getFileKey(): String?{
        if(fileKey == null) return null
        val attachmentsThatNeedDuplicate = attachments.filter { db.fileNeedsDuplicate(it.id) }
        return if(attachmentsThatNeedDuplicate.isNotEmpty() && attachments.containsAll(attachmentsThatNeedDuplicate)) {
            attachmentsThatNeedDuplicate.first().fileKey
        }else{
            fileKey
        }
    }

    private fun getEncryptedFileKeys(recipientId: String, deviceId: Int): List<String>?{
        if(attachments.isEmpty()) return null
        val fileKeys = mutableListOf<String>()
        attachments.forEach { fileKeys.add(signalClient.encryptMessage(recipientId, deviceId, getFileKey()!!).encryptedB64) }
        return fileKeys
    }

    private fun getFileKeys(): ArrayList<String>?{
        if(attachments.isEmpty()) return null
        val fileKeys: ArrayList<String> = ArrayList()
        attachments.mapTo(fileKeys) { it.fileKey }
        return fileKeys
    }

    private fun createCriptextAttachment(attachments: List<ComposerAttachment>)
            : List<PostEmailBody.CriptextAttachment> {
        val finalAttachments = mutableListOf<ComposerAttachment>()
        val attachmentsThatNeedDuplicate = attachments.filter { db.fileNeedsDuplicate(it.id) }
        if (attachmentsThatNeedDuplicate.isNotEmpty()) {
            finalAttachments.addAll(attachments.filter { it !in attachmentsThatNeedDuplicate })
            val op = Result.of { fileApiClient.duplicateAttachments(attachmentsThatNeedDuplicate.map { it.filetoken }) }
            if(op is Result.Success){
                val httpReturn = JSONObject(op.value.body).getJSONObject("duplicates")
                for(file in attachmentsThatNeedDuplicate){
                    db.updateFileToken(file.id, httpReturn.getString(file.filetoken))
                    finalAttachments.add(file)
                }
            }
        }else if(attachments.isNotEmpty()){
            finalAttachments.addAll(attachments)
        }
        return finalAttachments.map { attachment ->
            PostEmailBody.CriptextAttachment(token = attachment.filetoken,
                    name = FileUtils.getName(attachment.filepath), size = attachment.size, cid = attachment.cid)
        }
    }

    private val sendEmailOperation
            : (List<PostEmailBody.CriptextEmail>) -> Result<String, Exception> =
            { criptextEmails ->
                Result.of {
                    val requestBody = PostEmailBody(
                            threadId = EmailUtils.getThreadIdForSending(db, threadId, emailId),
                            subject = composerInputData.subject,
                            criptextEmails = criptextEmails,
                            guestEmail = guestEmails,
                            attachments = createCriptextAttachment(this.attachments))
                    apiClient.postEmail(requestBody).body
                }.mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
            }

    private val updateSentMailInDB: (String) -> Result<Unit, Exception> =
            { response ->
               Result.of {
                   val email = db.getEmailById(emailId)
                   val emailContent = EmailUtils.getEmailContentFromFileSystem(
                           filesDir = filesDir,
                           metadataKey = email!!.metadataKey,
                           recipientId = activeAccount.recipientId,
                           dbContent = email.content
                   )
                   val sentMailData = SentMailData.fromJSON(JSONObject(response))
                   db.updateEmailAndAddLabel(id = emailId, threadId = sentMailData.threadId,
                       messageId = sentMailData.messageId, metadataKey = sentMailData.metadataKey,
                       status = getDeliveryType(),
                       date = DateAndTimeUtils.getDateFromString(sentMailData.date, null)
                   )

                   EmailUtils.saveEmailInFileSystem(filesDir = filesDir, content = emailContent.first,
                           recipientId = activeAccount.recipientId, metadataKey = sentMailData.metadataKey,
                           headers = emailContent.second)

                   EmailUtils.deleteEmailInFileSystem(filesDir = filesDir,
                           metadataKey = email.metadataKey, recipientId = activeAccount.recipientId)
               }
            }


    override fun work(reporter: ProgressReporter<MailboxResult.SendMail>): MailboxResult.SendMail? {
        val mailRecipients = EmailUtils.getMailRecipients(composerInputData.to,
                composerInputData.cc, composerInputData.bcc, activeAccount.recipientId)
        val mailRecipientsNonCriptext = EmailUtils.getMailRecipientsNonCriptext(composerInputData.to,
                composerInputData.cc, composerInputData.bcc, activeAccount.recipientId)
        guestEmails = if(!mailRecipientsNonCriptext.isEmpty)
            getGuestEmails(mailRecipientsNonCriptext)
        else
            null

        val currentEmail = db.getEmailById(emailId)

        if(currentEmail != null && currentEmail.delivered == DeliveryTypes.SENT) return MailboxResult.SendMail.Success(null)

        val result = workOperation(mailRecipients)

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(result)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation(mailRecipients )
        else
            result

        return when (finalResult) {
            is Result.Success -> {
                db.increaseContactScore(listOf(emailId))
                MailboxResult.SendMail.Success(emailId)
            }
            is Result.Failure -> {
                catchException(finalResult.error)
            }
        }
    }

    private fun newRetryWithNewSessionOperation(mailRecipients: EmailUtils.MailRecipients)
            : Result<Unit, Exception> {
        val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient,
                activeAccount, storage, accountDao)
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when(refreshOperation){
            is Result.Success -> {
                val account = ActiveAccount.loadFromStorage(storage)!!
                fileApiClient.token = account.jwt
                apiClient.token = account.jwt
                workOperation(mailRecipients)
            }
            is Result.Failure -> {
                Result.of { throw refreshOperation.error }
            }
        }
    }

    private fun workOperation(mailRecipients: EmailUtils.MailRecipients): Result<Unit, Exception> = checkEncryptionKeysOperation(mailRecipients)
            .flatMap { encryptOperation(mailRecipients) }
            .flatMap(sendEmailOperation)
            .flatMap(updateSentMailInDB)

    private fun getGuestEmails(mailRecipientsNonCriptext: EmailUtils.MailRecipients) : PostEmailBody.GuestEmail?{
        val postGuestEmailBody: PostEmailBody.GuestEmail?
        if(composerInputData.passwordForNonCriptextUsers == null) {
            postGuestEmailBody = PostEmailBody.GuestEmail(mailRecipientsNonCriptext.toCriptext,
                    mailRecipientsNonCriptext.ccCriptext, mailRecipientsNonCriptext.bccCriptext,
                    HTMLUtils.addCriptextFooter(composerInputData.body), null, null, null, fileKey,
                    fileKeys = getFileKeys())
        }else {
            val tempSignalUser = getDummySignalSession(composerInputData.passwordForNonCriptextUsers)
            val sessionToEncrypt = getSignalSessionJSON(tempSignalUser,
                    tempSignalUser.fetchAPreKeyBundle()).toString().toByteArray()
            val (salt, iv, encryptedSession) =
                    AESUtil.encryptWithPassword(composerInputData.passwordForNonCriptextUsers, sessionToEncrypt)
            val encryptedBody = signalClient.encryptMessage(composerInputData.passwordForNonCriptextUsers,
                    1, composerInputData.body).encryptedB64
            val externalSession = EmailExternalSession(0, emailId = emailId, iv = iv, salt = salt,
                    encryptedBody = encryptedBody, encryptedSession = encryptedSession)
            db.saveExternalSession(externalSession)
            postGuestEmailBody = PostEmailBody.GuestEmail(mailRecipientsNonCriptext.toCriptext,
                    mailRecipientsNonCriptext.ccCriptext, mailRecipientsNonCriptext.bccCriptext,
                    encryptedBody, salt, iv, encryptedSession, null, fileKeys = getFileKeys())
            tempSignalUser.store.deleteAllSessions(composerInputData.passwordForNonCriptextUsers)
            rawSessionDao.deleteByRecipientId(composerInputData.passwordForNonCriptextUsers)
            rawIdentityKeyDao.deleteByRecipientId(composerInputData.passwordForNonCriptextUsers)

        }
        return postGuestEmailBody
    }

    private fun getDummySignalSession(recipientId: String): DummyUser{
        val keyGenerator = SignalKeyGenerator.Default(DeviceUtils.DeviceType.Android)
        val tempUser = InMemoryUser(keyGenerator, recipientId, 1).setup()
        val keyBundleFromTempUser = tempUser.fetchAPreKeyBundle()

        signalClient.createSessionsFromBundles(listOf(keyBundleFromTempUser))
        return tempUser
    }

    private fun getSignalSessionJSON(tempUser: DummyUser, keyBundleFromTempUser: PreKeyBundleShareData.DownloadBundle):JSONObject{
        val signedPreKey = tempUser.store.loadSignedPreKey(keyBundleFromTempUser.shareData.signedPreKeyId)

        val jsonReturn = JSONObject()
        val jsonIdentityKey = JSONObject()
        val jsonPreKey = JSONObject()
        val jsonSignedPreKey = JSONObject()
        jsonIdentityKey.put("publicKey", Encoding.byteArrayToString(tempUser.store.identityKeyPair.publicKey.serialize()))
        jsonIdentityKey.put("privateKey", Encoding.byteArrayToString(tempUser.store.identityKeyPair.privateKey.serialize()))
        jsonReturn.put("identityKey", jsonIdentityKey)
        jsonReturn.put("registrationId", tempUser.store.localRegistrationId)
        jsonPreKey.put("keyId", keyBundleFromTempUser.preKey?.id)
        jsonPreKey.put("publicKey", Encoding.byteArrayToString(tempUser.store.loadPreKey(keyBundleFromTempUser.preKey!!.id).keyPair.publicKey.serialize()))
        jsonPreKey.put("privateKey", Encoding.byteArrayToString(tempUser.store.loadPreKey(keyBundleFromTempUser.preKey.id).keyPair.privateKey.serialize()))
        jsonReturn.put("preKey", jsonPreKey)
        jsonSignedPreKey.put("keyId", keyBundleFromTempUser.shareData.signedPreKeyId)
        jsonSignedPreKey.put("publicKey", Encoding.byteArrayToString(signedPreKey.keyPair.publicKey.serialize()))
        jsonSignedPreKey.put("privateKey", Encoding.byteArrayToString(signedPreKey.keyPair.privateKey.serialize()))
        jsonReturn.put("signedPreKey", jsonSignedPreKey)
        jsonReturn.put("fileKey", fileKey)
        jsonReturn.put("fileKeys", JSONArray(getFileKeys()))
        return jsonReturn
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is JSONException -> UIMessage(resId = R.string.send_json_error)
            is ServerErrorException -> UIMessage(resId = R.string.send_bad_status, args = arrayOf(ex.errorCode))
            is NetworkErrorException -> UIMessage(resId = R.string.send_network_error)
            else -> UIMessage(resId = R.string.send_try_again_error)
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }
}