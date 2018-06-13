package com.email.scenes.mailbox.data

import android.accounts.NetworkErrorException
import com.email.R
import com.email.api.HttpClient
import com.email.api.HttpErrorHandlingHelper
import com.email.api.ServerErrorException
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.DeliveryTypes
import com.email.db.MailboxLocalDB
import com.email.db.dao.signal.RawSessionDao
import com.email.db.models.ActiveAccount
import com.email.db.models.Contact
import com.email.db.models.KnownAddress
import com.email.scenes.composer.data.ComposerAPIClient
import com.email.scenes.composer.data.ComposerAttachment
import com.email.scenes.composer.data.ComposerInputData
import com.email.scenes.composer.data.PostEmailBody
import com.email.signal.PreKeyBundleShareData
import com.email.signal.SignalClient
import com.email.utils.DateUtils
import com.email.utils.EmailAddressUtils.extractRecipientIdFromCriptextAddress
import com.email.utils.EmailAddressUtils.isFromCriptextDomain
import com.email.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by gabriel on 2/26/18.
 */

class SendMailWorker(private val signalClient: SignalClient,
                     private val rawSessionDao: RawSessionDao,
                     private val db: MailboxLocalDB,
                     httpClient: HttpClient,
                     activeAccount: ActiveAccount,
                     private val emailId: Long,
                     private val threadId: String?,
                     private val composerInputData: ComposerInputData,
                     private val attachments: List<ComposerAttachment>,
                     override val publishFn: (MailboxResult.SendMail) -> Unit)
    : BackgroundWorker<MailboxResult.SendMail> {
    override val canBeParallelized = false

    private val apiClient = ComposerAPIClient(httpClient, activeAccount.jwt)

    private fun getMailRecipients(): MailRecipients {
        val toAddresses = composerInputData.to.map(Contact.toAddress)
        val ccAddresses = composerInputData.cc.map(Contact.toAddress)
        val bccAddresses = composerInputData.bcc.map(Contact.toAddress)

        val toCriptext = toAddresses.filter(isFromCriptextDomain)
                                    .map(extractRecipientIdFromCriptextAddress)
        val ccCriptext = ccAddresses.filter(isFromCriptextDomain)
                                    .map(extractRecipientIdFromCriptextAddress)
        val bccCriptext = bccAddresses.filter(isFromCriptextDomain)
                                      .map(extractRecipientIdFromCriptextAddress)

        return MailRecipients(toCriptext = toCriptext, ccCriptext = ccCriptext,
                bccCriptext = bccCriptext)
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

    private fun addMissingSessions(criptextRecipients: List<String>) {
        val knownAddresses = findKnownAddresses(criptextRecipients)

        val findKeyBundlesResponse = apiClient.findKeyBundles(criptextRecipients, knownAddresses)
        val bundlesJSONArray = JSONArray(findKeyBundlesResponse)
        if (bundlesJSONArray.length() > 0) {
            val downloadedBundles = PreKeyBundleShareData.DownloadBundle.fromJSONArray(bundlesJSONArray)
            signalClient.createSessionsFromBundles(downloadedBundles)
        }
    }

    private fun encryptForCriptextRecipients(criptextRecipients: List<String>,
                                             availableAddresses: Map<String, List<Int>>,
                                             type: PostEmailBody.RecipientTypes)
            : List<PostEmailBody.CriptextEmail> {
        return criptextRecipients.map { recipientId ->
            val devices = availableAddresses[recipientId]
            if (devices == null || devices.isEmpty())
                throw IllegalArgumentException("Signal address for '$recipientId' does not exist in the store")
            devices.map { deviceId ->
                val encryptedData = signalClient.encryptMessage(recipientId, deviceId, composerInputData.body)
                PostEmailBody.CriptextEmail(recipientId = recipientId, deviceId = deviceId,
                        type = type, body = encryptedData.encryptedB64,
                        messageType = encryptedData.type)
            }
        }.flatten()
    }

    private fun createEncryptedEmails(mailRecipients: MailRecipients): List<PostEmailBody.CriptextEmail> {
        val knownCriptextAddresses = findKnownAddresses(mailRecipients.criptextRecipients)
        val criptextToEmails = encryptForCriptextRecipients(mailRecipients.toCriptext,
                knownCriptextAddresses, PostEmailBody.RecipientTypes.to)
        val criptextCcEmails = encryptForCriptextRecipients(mailRecipients.ccCriptext,
                knownCriptextAddresses, PostEmailBody.RecipientTypes.cc)
        val criptextBccEmails = encryptForCriptextRecipients(mailRecipients.bccCriptext,
                knownCriptextAddresses, PostEmailBody.RecipientTypes.bcc)
        return listOf(criptextToEmails, criptextCcEmails, criptextBccEmails).flatten()
    }

    override fun catchException(ex: Exception): MailboxResult.SendMail {
        val message = createErrorMessage(ex)
        return MailboxResult.SendMail.Failure(message)
    }

    private fun checkEncryptionKeysOperation(mailRecipients: MailRecipients)
            : Result<Unit, Exception> =
            Result.of { addMissingSessions(mailRecipients.criptextRecipients) }

    private fun encryptOperation(mailRecipients: MailRecipients)
            : Result<List<PostEmailBody.CriptextEmail>, Exception> =
            Result.of { createEncryptedEmails(mailRecipients) }

    private fun createCriptextAttachment(attachments: List<ComposerAttachment>)
            : List<PostEmailBody.CriptextAttachment> = attachments.map { attachment ->
        PostEmailBody.CriptextAttachment(token = attachment.filetoken)
    }

    private val sendEmailOperation
            : (List<PostEmailBody.CriptextEmail>) -> Result<String, Exception> =
            { criptextEmails ->
                Result.of {
                    val requestBody = PostEmailBody(
                            threadId = threadId,
                            subject = composerInputData.subject,
                            criptextEmails = criptextEmails,
                            guestEmail = null,
                            attachments = createCriptextAttachment(this.attachments))
                    apiClient.postEmail(requestBody)
                }.mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
            }

    private val updateSentMailInDB: (String) -> Result<Unit, Exception> =
            { response ->
               Result.of {
                   val sentMailData = SentMailData.fromJSON(JSONObject(response))
                   db.updateEmailAndAddLabelSent(id = emailId, threadId = sentMailData.threadId,
                       key = sentMailData.metadataKey.toString(), status = DeliveryTypes.SENT,
                       date = DateUtils.getDateFromString(sentMailData.date, null)
                   )
               }
            }


    override fun work(reporter: ProgressReporter<MailboxResult.SendMail>): MailboxResult.SendMail? {
        val mailRecipients = getMailRecipients()
        val result = checkEncryptionKeysOperation(mailRecipients)
                .flatMap { encryptOperation(mailRecipients) }
                .flatMap(sendEmailOperation)
                .flatMap(updateSentMailInDB)

        return when (result) {
            is Result.Success -> {
                MailboxResult.SendMail.Success(emailId)
            }
            is Result.Failure -> {
                val message = createErrorMessage(result.error)
                MailboxResult.SendMail.Failure(message)
            }
        }
    }
    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is JSONException -> UIMessage(resId = R.string.send_json_error)
            is ServerErrorException ->
                UIMessage(resId = R.string.send_bad_status, args = arrayOf(ex.errorCode))
            is NetworkErrorException -> UIMessage(resId = R.string.send_network_error)
            else -> UIMessage(resId = R.string.send_try_again_error)
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private class MailRecipients(val toCriptext: List<String>, val ccCriptext: List<String>,
                                 val bccCriptext: List<String>) {
        val criptextRecipients = listOf(toCriptext, ccCriptext, bccCriptext).flatten()
    }

}