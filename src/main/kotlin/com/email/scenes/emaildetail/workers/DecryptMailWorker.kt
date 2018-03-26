package com.email.scenes.emaildetail.workers

import com.email.R
import com.email.api.ServerErrorException
import com.email.bgworker.BackgroundWorker
import com.email.db.models.ActiveAccount
import com.email.scenes.emaildetail.data.EmailDetailResult
import com.email.signal.SignalClient
import com.email.utils.UIMessage
import com.github.kittinunf.result.Result

/**
 * Created by sebas on 3/26/18.
 */

class DecryptMailWorker(private val signalClient: SignalClient,
                        activeAccount: ActiveAccount,
                        private val emailId: Int,
                        private val recipientId: String,
                        private val deviceId: Int,
                        private val encryptedMessage: String,
                        override val publishFn: (EmailDetailResult.DecryptMail) -> Unit) : BackgroundWorker<EmailDetailResult.DecryptMail> {
    override val canBeParallelized = false


    override fun catchException(ex: Exception): EmailDetailResult.DecryptMail {
        val message = createErrorMessage(ex)
        return EmailDetailResult.DecryptMail.Failure(message, ex)
    }

    override fun work(): EmailDetailResult.DecryptMail? {
        val result = Result.of {
            signalClient.decryptMessage(
                    recipientId = recipientId,
                    deviceId = deviceId,
                    encryptedB64 = encryptedMessage
            )
        }

        return when (result) {
            is Result.Success -> EmailDetailResult.DecryptMail.Success(
                    decryptedText =  result.value
            )
            is Result.Failure -> {
                val message = createErrorMessage(result.error)
                EmailDetailResult.DecryptMail.Failure(message, result.error)
            }
        }
    }
    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is ServerErrorException ->
                UIMessage(resId = R.string.send_bad_status, args = arrayOf(ex.errorCode))
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
