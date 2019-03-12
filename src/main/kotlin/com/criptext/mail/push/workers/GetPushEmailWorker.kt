package com.criptext.mail.push.workers

import android.content.res.Resources
import com.criptext.mail.R
import com.criptext.mail.api.EmailInsertionAPIClient
import com.criptext.mail.api.Hosts
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.EmailMetadata
import com.criptext.mail.api.models.Event
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.push.data.PushResult
import com.criptext.mail.scenes.mailbox.data.MailboxAPIClient
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.EventHelper
import com.criptext.mail.utils.EventLoader
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.squareup.picasso.Picasso
import org.whispersystems.libsignal.DuplicateMessageException
import java.io.IOException


class GetPushEmailWorker(
        private val signalClient: SignalClient,
        private val dbEvents: EventLocalDB,
        private val activeAccount: ActiveAccount,
        private val label: Label,
        private val pushData: Map<String, String>,
        private val shouldPostNotification: Boolean,
        httpClient: HttpClient,
        override val publishFn: (
                PushResult.NewEmail) -> Unit)
    : BackgroundWorker<PushResult.NewEmail> {


    override val canBeParallelized = false

    private val apiClient = MailboxAPIClient(httpClient, activeAccount.jwt)
    private val emailInsertionApiClient = EmailInsertionAPIClient(httpClient, activeAccount.jwt)
    private val eventsToAcknowldege = mutableListOf<Long>()

    override fun catchException(ex: Exception): PushResult.NewEmail {
        val message = createErrorMessage(ex)
        return PushResult.NewEmail.Failure(label, message, ex, pushData, shouldPostNotification)
    }

    private fun processFailure(failure: Result.Failure<Boolean,
            Exception>): PushResult.NewEmail {
        return if (failure.error is EventHelper.NothingNewException)
            PushResult.NewEmail.Success(
                    mailboxLabel = label,
                    isManual = true,
                    shouldPostNotification = shouldPostNotification,
                    pushData = pushData,
                    senderImage = null)
        else
            PushResult.NewEmail.Failure(
                    mailboxLabel = label,
                    message = createErrorMessage(failure.error),
                    exception = failure.error,
                    pushData = pushData,
                    shouldPostNotification = shouldPostNotification)
    }

    override fun work(reporter: ProgressReporter<PushResult.NewEmail>)
            : PushResult.NewEmail? {

        val rowId = pushData["rowId"]?.toInt() ?: return PushResult.NewEmail.Failure(
                mailboxLabel = label,
                message = createErrorMessage(EventHelper.NothingNewException()),
                exception = EventHelper.NothingNewException(),
                pushData = pushData,
                shouldPostNotification = shouldPostNotification)

        val requestEvents = EventLoader.getEvent(apiClient, rowId)
        val operationResult = requestEvents
                .flatMap(processEvent)

        val newData = mutableMapOf<String, String>()
        newData.putAll(pushData)


        return when(operationResult) {
            is Result.Success -> {
                val metadataKey = newData["metadataKey"]?.toLong()
                if(metadataKey != null) {
                    val email = dbEvents.getEmailByMetadataKey(metadataKey)
                    if(email != null){
                        val files = dbEvents.getFullEmailById(emailId = email.id)!!.files
                        newData["preview"] = email.preview
                        newData["subject"] = email.subject
                        newData["hasInlineImages"] = (files.firstOrNull { it.cid != null }  != null).toString()
                        newData["name"] = dbEvents.getFromContactByEmailId(email.id)[0].name
                        newData["email"] = dbEvents.getFromContactByEmailId(email.id)[0].email
                        val emailAddress = newData["email"]
                        val bm = try {
                            if(emailAddress != null && EmailAddressUtils.isFromCriptextDomain(emailAddress))
                                Picasso.get().load(Hosts.restApiBaseUrl
                                        .plus("/user/avatar/${EmailAddressUtils.extractRecipientIdFromCriptextAddress(emailAddress)}")).get()
                            else
                                null
                        } catch (ex: Exception){
                            null
                        }
                        PushResult.NewEmail.Success(
                                mailboxLabel = label,
                                isManual = true,
                                pushData = newData,
                                shouldPostNotification = shouldPostNotification,
                                senderImage = bm
                        )
                    }else{
                        PushResult.NewEmail.Failure(
                                mailboxLabel = label,
                                message = createErrorMessage(Resources.NotFoundException()),
                                exception = Resources.NotFoundException(),
                                pushData = pushData,
                                shouldPostNotification = shouldPostNotification)
                    }
                }else {
                    PushResult.NewEmail.Failure(
                            mailboxLabel = label,
                            message = createErrorMessage(Resources.NotFoundException()),
                            exception = Resources.NotFoundException(),
                            pushData = pushData,
                            shouldPostNotification = shouldPostNotification)
                }
            }

            is Result.Failure -> processFailure(operationResult)
        }
    }

    val processEvent: (Event) -> Result<Boolean, Exception> = { event ->
        Result.of {
            val shouldReload = processNewEmails(event)
            shouldReload.or(acknowledgeEventsIgnoringErrors(eventsToAcknowldege))
        }
    }

    private fun processNewEmails(event: Event): Boolean {
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

        val eventIdsToAcknowledge = listOf(event)
                .map(toIdAndMetadataPair)
                .filter(emailInsertedSuccessfully)
                .map(toEventId)

        if (eventIdsToAcknowledge.isNotEmpty())
            eventsToAcknowldege.addAll(eventIdsToAcknowledge)

        return eventIdsToAcknowledge.isNotEmpty()
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private fun insertIncomingEmailTransaction(metadata: EmailMetadata) =
            dbEvents.insertIncomingEmail(signalClient, emailInsertionApiClient, metadata, activeAccount)

    private fun updateExistingEmailTransaction(metadata: EmailMetadata) =
            dbEvents.updateExistingEmail(metadata, activeAccount)

    private fun acknowledgeEventsIgnoringErrors(eventIdsToAcknowledge: List<Long>): Boolean {
        try {
            if(eventIdsToAcknowledge.isNotEmpty())
                apiClient.acknowledgeEvents(eventIdsToAcknowledge)
        } catch (ex: IOException) {
            // if this request fails, just ignore it, we can acknowledge again later
        }
        return eventIdsToAcknowledge.isNotEmpty()
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
}
