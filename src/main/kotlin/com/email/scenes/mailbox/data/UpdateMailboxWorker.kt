package com.email.scenes.mailbox.data

import com.email.R
import com.email.api.HttpErrorHandlingHelper
import com.email.api.ServerErrorException
import com.email.api.models.EmailMetadata
import com.email.api.models.Event
import com.email.bgworker.BackgroundWorker
import com.email.db.*
import com.email.db.dao.EmailInsertionDao
import com.email.db.models.*
import com.email.signal.SignalClient
import com.email.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONArray
import org.whispersystems.libsignal.DuplicateMessageException

/**
 * Created by sebas on 3/22/18.
 */

class UpdateMailboxWorker(
        private val signalClient: SignalClient,
        private val db: MailboxLocalDB,
        private val dao: EmailInsertionDao,
        activeAccount: ActiveAccount,
        private val label: Label,
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
            try {
                apiClient.getPendingEvents()
            }  catch (ex: ServerErrorException) {
                // not found means no new pending events
                if (ex.errorCode == 404) "[]" else throw ex
            }
        }
    }

    private fun processFailure(failure: Result.Failure<List<EmailThread>, Exception>) =
            if (failure.error is NothingNewException)
                MailboxResult.UpdateMailbox.Success(
                        mailboxLabel = label,
                        isManual = true,
                        mailboxThreads = null)
            else
                    MailboxResult.UpdateMailbox.Failure(
                    label,
                    createErrorMessage(failure.error))

    override fun work(): MailboxResult.UpdateMailbox? {
        val operationResult = fetchPendingEvents()
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
                .flatMap(parseEvents)
                .flatMap(processEvents)

        return when(operationResult) {
            is Result.Success -> {
                return MailboxResult.UpdateMailbox.Success(
                        mailboxLabel = label,
                        isManual = true,
                        mailboxThreads = operationResult.value
                )
            }

            is Result.Failure -> processFailure(operationResult)
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

    private val parseEvents: (String) -> Result<List<Event>, Exception> = { jsonString ->
        Result.of {
            val eventsJSONArray = JSONArray(jsonString)
            val lastIndex = eventsJSONArray.length() - 1
            if (lastIndex > -1) {
                (0..lastIndex).map {
                    val eventJSONString = eventsJSONArray.get(it).toString()
                    Event.fromJSON(eventJSONString)
                }
            } else emptyList()

        }
    }

    private fun decryptMessage(recipientId: String, deviceId: Int, encryptedB64: String): String {
        return try {
            signalClient.decryptMessage(recipientId = recipientId,
                    deviceId = deviceId,
                    encryptedB64 = encryptedB64)
        } catch (ex: Exception) {
            "Unable to decrypt message."
        }
    }

    private fun reloadMailbox(newEmailCount: Int): List<EmailThread> {
        return if (newEmailCount > 0)
            db.getEmailsFromMailboxLabel(labelTextTypes = label.text, oldestEmailThread = null,
                    limit = 20, rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(label))
        else throw NothingNewException()
    }


    private fun processNewEmails(events: List<Event>): Int {
        val newEmails = events
            .filter({ it.cmd == Event.Cmd.newEmail })

        newEmails
            .map({ EmailMetadata.fromJSON(it.params) })
            .forEach { metadata ->
                val incomingEmailLabels = listOf(Label.defaultItems.inbox)
                dao.runTransaction(Runnable {
                    val body = apiClient.getBodyFromEmail(metadata.bodyKey)
                    val decryptedBody = decryptMessage(recipientId = metadata.fromRecipientId,
                            deviceId = 1, encryptedB64 = body)
                    EmailInsertionSetup.exec(dao, metadata, decryptedBody, incomingEmailLabels)
                })
            }

        return newEmails.size
    }



    private val processEvents: (List<Event>) -> Result<List<EmailThread>, Exception> = { events ->
        Result.of {
            val newEmailCount = processNewEmails(events)
            reloadMailbox(newEmailCount)
        }
    }

    private class NothingNewException: Exception()
}
