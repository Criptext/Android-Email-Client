package com.email.scenes.mailbox.data

import android.util.Log
import com.email.R
import com.email.api.EmailInsertionAPIClient
import com.email.api.HttpClient
import com.email.api.HttpErrorHandlingHelper
import com.email.api.models.EmailMetadata
import com.email.api.models.Event
import com.email.api.models.TrackingUpdate
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.*
import com.email.db.dao.EmailDao
import com.email.db.dao.EmailInsertionDao
import com.email.db.models.*
import com.email.email_preview.EmailPreview
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
        private val emailDao: EmailDao,
        private val dao: EmailInsertionDao,
        private val activeAccount: ActiveAccount,
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

    private fun processFailure(failure: Result.Failure<List<EmailPreview>, Exception>): MailboxResult.UpdateMailbox {
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

    override fun work(reporter: ProgressReporter<MailboxResult.UpdateMailbox>)
            : MailboxResult.UpdateMailbox? {
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

    private fun reloadMailbox(shouldReload: Boolean): List<EmailPreview> {
        return if (shouldReload)
            db.getThreadsFromMailboxLabel(
                    labelTextTypes = label.text,
                    startDate = null,
                    limit = Math.max(20, loadedThreadsCount),
                    rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(label),
                    userEmail = activeAccount.userEmail)
                    .map { EmailPreview.fromEmailThread(it) }
        else throw NothingNewException()
    }

    private fun insertIncomingEmailTransaction(metadata: EmailMetadata) =
            EmailInsertionSetup.insertIncomingEmailTransaction(signalClient = signalClient,
                            dao = dao, apiClient = emailInsertionApiClient, metadata = metadata,
                    activeAccount = activeAccount)

    private fun updateExistingEmailTransaction(metadata: EmailMetadata) =
            ExistingEmailUpdateSetup.updateExistingEmailTransaction(metadata = metadata, dao = dao,
                    activeAccount = activeAccount)

    private fun acknowledgeEventsIgnoringErrors(eventIdsToAcknowledge: List<Long>) {
        try {
            apiClient.acknowledgeEvents(eventIdsToAcknowledge)
        } catch (ex: IOException) {
            // if this request fails, just ignore it, we can acknowledge again later
        }
    }

    private fun markEmailsAsOpened(eventIds: List<Long>, metadataKeys: List<Long>): Boolean {
        if (metadataKeys.isNotEmpty()) {
            emailDao.changeDeliveryTypeByMetadataKey(metadataKeys, DeliveryTypes.READ)
            acknowledgeEventsIgnoringErrors(eventIds)
            return true
        }
        return false
    }

    private fun processTrackingUpdates(events: List<Event>): Boolean {
        val isTrackingUpdateEvent: (Event) -> Boolean = { it.cmd == Event.Cmd.trackingUpdate }
        val toIdAndTrackingUpdatePair: (Event) -> Pair<Long, TrackingUpdate> = {
            Pair(it.rowid, TrackingUpdate.fromJSON(it.params))
        }

        val trackingUpdates = events.filter(isTrackingUpdateEvent)
                .map(toIdAndTrackingUpdatePair)

        // assume all tracking updates are open updates for now
        val openUpdates = trackingUpdates
        val metadataKeysOfReadEmails = openUpdates.map { (_, open) -> open.metadataKey }
        val eventIdsToAcknowledge = openUpdates.map { it.first }

        return markEmailsAsOpened(eventIds = eventIdsToAcknowledge,
                           metadataKeys = metadataKeysOfReadEmails)
    }

    private fun processNewEmails(events: List<Event>): Boolean {
        val isNewEmailEvent: (Event) -> Boolean = { it.cmd == Event.Cmd.newEmail }
        val toIdAndMetadataPair: (Event) -> Pair<Long, EmailMetadata> =
                { Pair( it.rowid, EmailMetadata.fromJSON(it.params)) }
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
                    if(ex is DuplicateMessageException){
                        updateExistingEmailTransaction(metadata)
                    }
                    ex is DuplicateMessageException
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

        return eventIdsToAcknowledge.isNotEmpty()
    }



    private val processEvents: (List<Event>) -> Result<List<EmailPreview>, Exception> = { events ->
        Result.of {
            val shouldReload = processTrackingUpdates(events)
                            || processNewEmails(events)
            reloadMailbox(shouldReload)
        }
    }

    private class NothingNewException: Exception()
}
