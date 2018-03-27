package com.email.scenes.mailbox.data

import com.email.R
import com.email.api.HttpErrorHandlingHelper
import com.email.bgworker.BackgroundWorker
import com.email.db.DeliveryTypes
import com.email.db.MailboxLocalDB
import com.email.db.models.ActiveAccount
import com.email.db.models.Email
import com.email.signal.SignalClient
import com.email.utils.DateUtils
import com.email.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONArray
import org.json.JSONObject
import org.whispersystems.libsignal.DuplicateMessageException

/**
 * Created by sebas on 3/22/18.
 */

class UpdateMailboxWorker(
        private val signalClient: SignalClient,
        private val db: MailboxLocalDB,
        private val activeAccount: ActiveAccount,
        private val label: String,
        override val publishFn: (
                MailboxResult.UpdateMailbox) -> Unit)
    : BackgroundWorker<MailboxResult.UpdateMailbox> {

    private val apiClient = MailboxAPIClient(activeAccount.jwt)
    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.UpdateMailbox {

        val message = createErrorMessage(ex)
        return MailboxResult.UpdateMailbox.Failure(label, message)
    }
    private fun fetchPendingEvents():Result<String, Exception> {
        return Result.of {
            apiClient.getPendingEvents()
        }
    }
    private val parseEmails: (input: String) -> Result<List<EmailThread>, Exception> = {
        input ->
        Result.of {
            parseContent(input = input)
        }.mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
    }
    override fun work(): MailboxResult.UpdateMailbox? {
        val operationResult = fetchPendingEvents().
                flatMap(parseEmails).
                mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)

        return when(operationResult) {
            is Result.Success -> {
                return MailboxResult.UpdateMailbox.Success(
                        mailboxLabel = "INBOX",
                        isManual = true,
                        mailboxThreads = operationResult.value
                )
            }

            is Result.Failure -> MailboxResult.UpdateMailbox.Failure(
                    label, createErrorMessage(operationResult.error))
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when(ex) {
            is DuplicateMessageException ->
                UIMessage(resId = R.string.email_already_decrypted)
            else -> {
                UIMessage(resId = R.string.failed_getting_emails)
            }
        }
    }


    private fun parseContent(input: String): List<EmailThread> {
        val jsonArray = JSONArray(input)
        for(i in 0 until jsonArray.length()) {
            val fullData = JSONObject(jsonArray.get(i).toString())
            val emailData = JSONObject(fullData.getString("params"))
            val from = emailData.getString("from")
            val to = emailData.getString("to")
            val bodyKey =  emailData.getString("bodyKey")
            val resultOperationS3 = Result.of {
                apiClient.getBodyFromEmail(bodyKey)
            }
            when(resultOperationS3) {
                is Result.Success -> {

                    val bodyContent = signalClient.decryptMessage(
                            recipientId = from,
                            deviceId = 1,
                            encryptedB64 = resultOperationS3.value)
                    val email = Email(
                            id=null,
                            unread = true,
                            date = DateUtils.getDateFromString(
                                    emailData.getString("date"),
                                    null),
                            threadid = emailData.getString("threadId"),
                            subject = emailData.getString("subject"),
                            isTrash = false,
                            secure = true,
                            preview = emailData.getString("preview"),
                            key = bodyKey,
                            isDraft = false,
                            delivered = DeliveryTypes.RECEIVED,
                            content = bodyContent
                    )
                    val insertedEmailId = db.addEmail(email)
                    db.createContactFrom(from, insertedEmailId)
                }
            }
        }
        return db.getNotArchivedEmailThreads()
    }
}
