package com.email.scenes.composer.data

import android.accounts.NetworkErrorException
import com.email.R
import com.email.api.HttpErrorHandlingHelper
import com.email.api.ServerErrorException
import com.email.bgworker.BackgroundWorker
import com.email.db.dao.signal.RawSessionDao
import com.email.db.models.ActiveAccount
import com.email.db.models.Contact
import com.email.db.models.KnownAddress
import com.email.scenes.composer.ui.UIData
import com.email.signal.PreKeyBundleShareData
import com.email.signal.SignalClient
import com.email.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONArray
import org.json.JSONException

/**
 * Created by gabriel on 2/26/18.
 */

class SendMailWorker(private val signalClient: SignalClient,
                     private val rawSessionDao: RawSessionDao,
                     activeAccount: ActiveAccount,
                     private val uiData: UIData,
                     override val publishFn: (ComposerResult.SendMail) -> Unit) : BackgroundWorker<ComposerResult.SendMail> {
    override val canBeParallelized = false

    private val apiClient = ComposerAPIClient(activeAccount.jwt)

    private fun getMailRecipients(): MailRecipients {
        val toAddresses = uiData.to.map(Contact.toName)
        val ccAddresses = uiData.cc.map(Contact.toName)
        val bccAddresses = uiData.bcc.map(Contact.toName)

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
                val encryptedBody = signalClient.encryptMessage(recipientId, deviceId, uiData.body)
                PostEmailBody.CriptextEmail(recipientId = recipientId, deviceId = deviceId,
                        type = type, body = encryptedBody)
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

    override fun catchException(ex: Exception): ComposerResult.SendMail {
        val message = createErrorMessage(ex)
        return ComposerResult.SendMail.Failure(message)
    }

    private fun checkEncryptionKeysOperation(mailRecipients: MailRecipients): Result<Unit, Exception> =
            Result.of { addMissingSessions(mailRecipients.criptextRecipients) }

    private fun encryptOperation(mailRecipients: MailRecipients): Result<List<PostEmailBody.CriptextEmail>, Exception> =
            Result.of { createEncryptedEmails(mailRecipients) }

    private val sendEmailOperation: (List<PostEmailBody.CriptextEmail>) -> Result<String, Exception> =
            { criptextEmails ->
                Result.of {
                    val requestBody = PostEmailBody(
                            subject = uiData.subject,
                            criptextEmails = criptextEmails,
                            guestEmail = null)
                    apiClient.postEmail(requestBody)
                }.mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
            }

    override fun work(): ComposerResult.SendMail? {
        val mailRecipients = getMailRecipients()
        val result = checkEncryptionKeysOperation(mailRecipients)
                .flatMap { encryptOperation(mailRecipients) }
                .flatMap(sendEmailOperation)

        return when (result) {
            is Result.Success -> ComposerResult.SendMail.Success()
            is Result.Failure -> {
                val message = createErrorMessage(result.error)
                ComposerResult.SendMail.Failure(message)
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




    private companion object {
        val CRIPTEXT_DOMAIN_SUFFIX = "@jigl.com"
        private val isFromCriptextDomain:(String) -> Boolean =
                { address -> address.endsWith(CRIPTEXT_DOMAIN_SUFFIX) }
        private val extractRecipientIdFromCriptextAddress: (String) -> String =
                { address -> address.substring(0, address.length - CRIPTEXT_DOMAIN_SUFFIX.length) }
    }



}