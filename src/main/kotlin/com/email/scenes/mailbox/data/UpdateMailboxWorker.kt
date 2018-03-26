package com.email.scenes.mailbox.data

import android.util.Log
import com.email.R
import com.email.api.HttpErrorHandlingHelper
import com.email.bgworker.BackgroundWorker
import com.email.db.MailboxLocalDB
import com.email.db.models.ActiveAccount
import com.email.db.models.Email
import com.email.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by sebas on 3/22/18.
 */

class UpdateMailboxWorker(
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

    override fun work(): MailboxResult.UpdateMailbox? {
        val operationResult =  Result.of {
            apiClient.getPendingEvents()
        }.mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)

        return when(operationResult) {
            is Result.Success -> {
                return MailboxResult.UpdateMailbox.Success(
                        mailboxLabel = "INBOX", // temporal
                        isManual = true, // temporal
                        mailboxThreads = parseValue(operationResult.value)
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
        UIMessage(resId = R.string.failed_getting_emails)
    }

    fun parseValue(input: String): List<EmailThread> {
        var sdf : SimpleDateFormat = SimpleDateFormat( "yyyy-MM-dd HH:mm:dd")
        val jsonArray = JSONArray(input)
        for(i in 0 until jsonArray.length()) {
            val fullData = JSONObject(jsonArray.get(i).toString())
            val emailData = JSONObject(fullData.getString("params"))
            Log.d("data", emailData.toString())
            val email = Email(
                    id=null,
                    unread = true,
                    date = sdf.parse(emailData.getString("date")),
                    threadid = emailData.getString("threadId"),
                    subject = emailData.getString("subject"),
                    isTrash = false,
                    secure = true,
                    preview = emailData.getString("preview"),
                    key = emailData.getString("bodyKey"),
                    isDraft = false,
                    delivered = 0 ,
                    content = ""
                    )
            db.addEmail(email)
        }
        return db.getNotArchivedEmailThreads()
    }
}
