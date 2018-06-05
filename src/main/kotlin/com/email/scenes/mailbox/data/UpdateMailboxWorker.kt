package com.email.scenes.mailbox.data

import com.email.R
import com.email.api.EmailInsertionAPIClient
import com.email.api.HttpClient
import com.email.api.HttpErrorHandlingHelper
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
import java.io.IOException

/**
 * Created by sebas on 3/22/18.
 */

class UpdateMailboxWorker(
        private val signalClient: SignalClient,
        private val db: MailboxLocalDB,
        private val dao: EmailInsertionDao,
        activeAccount: ActiveAccount,
        private val loadedThreadsCount: Int,
        private val label: Label,
        httpClient: HttpClient,
        override val publishFn: (
                MailboxResult.UpdateMailbox) -> Unit)
    : BackgroundWorker<MailboxResult.UpdateMailbox> {

    private val apiClient = MailboxAPIClient(httpClient, activeAccount.jwt)
    private val emailInsertionApiClient = EmailInsertionAPIClient(httpClient, activeAccount.jwt)
    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.UpdateMailbox {
        val message = createErrorMessage(ex)
        return MailboxResult.UpdateMailbox.Failure(label, message, ex)
    }
    private fun fetchPendingEvents():Result<String, Exception> {
        return Result.of {
            val responseText = apiClient.getPendingEvents()
            if (responseText.isEmpty()) "[]" else responseText
        }
    }

    private fun processFailure(failure: Result.Failure<List<EmailThread>, Exception>): MailboxResult.UpdateMailbox {
        return if (failure.error is NothingNewException)
            MailboxResult.UpdateMailbox.Success(
                    mailboxLabel = label,
                    isManual = true,
                    mailboxThreads = null)
        else
            MailboxResult.UpdateMailbox.Failure(
                    mailboxLabel = label,
                    message = createErrorMessage(failure.error),
                    exception = failure.error)
    }

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

    private fun reloadMailbox(newEmailCount: Int): List<EmailThread> {
        return if (newEmailCount > 0)
            db.getEmailsFromMailboxLabel(labelTextTypes = label.text, oldestEmailThread = null,
                    limit = Math.max(20, loadedThreadsCount),
                    rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(label))
        else throw NothingNewException()
    }

    private fun insertIncomingEmailTransaction(metadata: EmailMetadata) =
            EmailInsertionSetup.insertIncomingEmailTransaction(signalClient = signalClient,
                            dao = dao, apiClient = emailInsertionApiClient, metadata = metadata)

    private fun acknowledgeEventsIgnoringErrors(eventIdsToAcknowledge: List<Long>) {
        try {
            apiClient.acknowledgeEvents(eventIdsToAcknowledge)
        } catch (ex: IOException) {
            // if this request fails, just ignore it, we can acknowledge again later
        }
    }

    private fun processNewEmails(events: List<Event>): Int {
        val isNewEmailEvent: (Event) -> Boolean = { it.cmd == Event.Cmd.newEmail }
        val toIdAndMetadataPair: (Event) -> Pair<Long, EmailMetadata> =
                { Pair( it.rowid,  EmailMetadata.fromJSON(it.params)) }
        val emailInsertedSuccessfully: (Pair<Long, EmailMetadata>) -> Boolean =
            { (_, metadata) ->
                try {
                    insertIncomingEmailTransaction(metadata)
                    // insertion success, try to acknowledge it
                    true
                } catch (ex: DuplicateMessageException) {
                    // duplicated, try to acknowledge it
                    true
                }
                catch (ex: Exception) {
                    // Unknown exception, probably network related, skip acknowledge
                    false
                }
            }
        val toEventId: (Pair<Long, EmailMetadata>) -> Long =
                { (eventId, _) -> eventId }

        val eventIdsToAcknowledge = events
            .filter(isNewEmailEvent)
            .map(toIdAndMetadataPair)
            .filter(emailInsertedSuccessfully)
            .map(toEventId)

        if (eventIdsToAcknowledge.isNotEmpty())
            acknowledgeEventsIgnoringErrors(eventIdsToAcknowledge)

        return eventIdsToAcknowledge.size
    }



    private val processEvents: (List<Event>) -> Result<List<EmailThread>, Exception> = { events ->
        Result.of {
            val newEmailCount = processNewEmails(events)
            reloadMailbox(newEmailCount)
        }
    }

    private class NothingNewException: Exception()
}
