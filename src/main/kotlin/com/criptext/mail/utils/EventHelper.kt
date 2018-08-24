package com.criptext.mail.utils

import com.criptext.mail.api.EmailInsertionAPIClient
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.models.*
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.mailbox.data.MailboxAPIClient
import com.criptext.mail.signal.SignalClient
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONArray
import org.whispersystems.libsignal.DuplicateMessageException
import java.io.IOException

class EventHelper(private val db: EventLocalDB,
                  httpClient: HttpClient,
                  private val activeAccount: ActiveAccount,
                  private val signalClient: SignalClient,
                  private val acknoledgeEvents: Boolean){

    private val mailboxAPIClient = MailboxAPIClient(httpClient, activeAccount.jwt)
    private val emailInsertionApiClient = EmailInsertionAPIClient(httpClient, activeAccount.jwt)
    private val eventsToAcknowldege = mutableListOf<Long>()

    private lateinit var label: Label
    private var loadedThreadsCount: Int? = null

    fun setupForMailbox(label: Label, threadCount: Int?){
        this.label = label
        loadedThreadsCount = threadCount
    }


    fun processAllPendingEvents():Result<List<EmailPreview>,Exception>{
        return fetchPendingEvents()
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
                .flatMap(parseEvents)
                .flatMap(processEvents)
    }


    fun fetchPendingEvents():Result<String, Exception> {
        return Result.of {
            val responseText = mailboxAPIClient.getPendingEvents()
            if (responseText.isEmpty()) "[]" else responseText
        }
    }

    val parseEvents: (String) -> Result<List<Event>, Exception> = { jsonString ->
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

    val processEvents: (List<Event>) -> Result<List<EmailPreview>, Exception> = { events ->
        Result.of {
            val shouldReload = processTrackingUpdates(events).or(processNewEmails(events))
                    .or(processThreadReadStatusChanged(events)).or(processUnsendEmailStatusChanged(events))
                    .or(processPeerUsernameChanged(events)).or(processEmailLabelChanged(events))
                    .or(processThreadLabelChanged(events)).or(processEmailDeletedPermanently(events))
                    .or(processThreadDeletedPermanently(events)).or(processLabelCreated(events))
                    .or(processOnError(events)).or(processEmailReadStatusChanged(events))
            reloadMailbox(shouldReload.or(acknowledgeEventsIgnoringErrors(eventsToAcknowldege)))
        }
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

        if (eventIdsToAcknowledge.isNotEmpty() && acknoledgeEvents)
            eventsToAcknowldege.addAll(eventIdsToAcknowledge)

        return eventIdsToAcknowledge.isNotEmpty()
    }

    private fun processThreadReadStatusChanged(events: List<Event>): Boolean {
        val isThreadReadStatusChangedEvent: (Event) -> Boolean = { it.cmd == Event.Cmd.peerEmailThreadReadStatusUpdate }
        val toIdAndMetadataPair: (Event) -> Pair<Long, PeerReadThreadStatusUpdate> =
                { Pair( it.rowid, PeerReadThreadStatusUpdate.fromJSON(it.params)) }
        val emailInsertedSuccessfully: (Pair<Long, PeerReadThreadStatusUpdate>) -> Boolean =
                { (_, metadata) ->
                    try {
                        updateThreadReadStatus(metadata)
                        // insertion success, try to acknowledge it
                        true
                    }
                    catch (ex: Exception) {
                        false
                    }
                }
        val toEventId: (Pair<Long, PeerReadThreadStatusUpdate>) -> Long =
                { (eventId, _) -> eventId }

        val eventIdsToAcknowledge = events
                .filter(isThreadReadStatusChangedEvent)
                .map(toIdAndMetadataPair)
                .filter(emailInsertedSuccessfully)
                .map(toEventId)

        if (eventIdsToAcknowledge.isNotEmpty() && acknoledgeEvents)
            eventsToAcknowldege.addAll(eventIdsToAcknowledge)

        return eventIdsToAcknowledge.isNotEmpty()
    }

    private fun processEmailReadStatusChanged(events: List<Event>): Boolean {
        val isEmailReadStatusChangedEvent: (Event) -> Boolean = { it.cmd == Event.Cmd.peerEmailReadStatusUpdate }
        val toIdAndMetadataPair: (Event) -> Pair<Long, PeerReadEmailStatusUpdate> =
                { Pair( it.rowid, PeerReadEmailStatusUpdate.fromJSON(it.params)) }
        val emailInsertedSuccessfully: (Pair<Long, PeerReadEmailStatusUpdate>) -> Boolean =
                { (_, metadata) ->
                    try {
                        updateEmailReadStatus(metadata)
                        // insertion success, try to acknowledge it
                        true
                    }
                    catch (ex: Exception) {
                        false
                    }
                }
        val toEventId: (Pair<Long, PeerReadEmailStatusUpdate>) -> Long =
                { (eventId, _) -> eventId }

        val eventIdsToAcknowledge = events
                .filter(isEmailReadStatusChangedEvent)
                .map(toIdAndMetadataPair)
                .filter(emailInsertedSuccessfully)
                .map(toEventId)

        if (eventIdsToAcknowledge.isNotEmpty() && acknoledgeEvents)
            eventsToAcknowldege.addAll(eventIdsToAcknowledge)

        return eventIdsToAcknowledge.isNotEmpty()
    }

    private fun processUnsendEmailStatusChanged(events: List<Event>): Boolean {
        val isEmailUnsendStatusChangedEvent: (Event) -> Boolean = { it.cmd == Event.Cmd.peerEmailUnsendStatusUpdate }
        val toIdAndMetadataPair: (Event) -> Pair<Long, PeerUnsendEmailStatusUpdate> =
                { Pair( it.rowid, PeerUnsendEmailStatusUpdate.fromJSON(it.params)) }
        val emailUnsentSuccessfully: (Pair<Long, PeerUnsendEmailStatusUpdate>) -> Boolean =
                { (_, metadata) ->
                    try {
                        updateUnsendEmailStatus(metadata)
                        // insertion success, try to acknowledge it
                        true
                    }catch (ex: Exception) {
                        false
                    }
                }
        val toEventId: (Pair<Long, PeerUnsendEmailStatusUpdate>) -> Long =
                { (eventId, _) -> eventId }

        val eventIdsToAcknowledge = events
                .filter(isEmailUnsendStatusChangedEvent)
                .map(toIdAndMetadataPair)
                .filter(emailUnsentSuccessfully)
                .map(toEventId)

        if (eventIdsToAcknowledge.isNotEmpty() && acknoledgeEvents)
            eventsToAcknowldege.addAll(eventIdsToAcknowledge)

        return eventIdsToAcknowledge.isNotEmpty()
    }

    private fun processPeerUsernameChanged(events: List<Event>): Boolean {
        val isUsernameChangedEvent: (Event) -> Boolean = { it.cmd == Event.Cmd.peerUserChangeName }
        val toIdAndMetadataPair: (Event) -> Pair<Long, PeerUsernameChangedStatusUpdate> =
                { Pair( it.rowid, PeerUsernameChangedStatusUpdate.fromJSON(it.params)) }
        val usernameChangedSuccessfully: (Pair<Long, PeerUsernameChangedStatusUpdate>) -> Boolean =
                { (_, metadata) ->
                    try {
                        updateUsernameStatus(metadata)
                        // insertion success, try to acknowledge it
                        true
                    }catch (ex: Exception) {
                        false
                    }
                }
        val toEventId: (Pair<Long, PeerUsernameChangedStatusUpdate>) -> Long =
                { (eventId, _) -> eventId }

        val eventIdsToAcknowledge = events
                .filter(isUsernameChangedEvent)
                .map(toIdAndMetadataPair)
                .filter(usernameChangedSuccessfully)
                .map(toEventId)

        if (eventIdsToAcknowledge.isNotEmpty() && acknoledgeEvents)
            eventsToAcknowldege.addAll(eventIdsToAcknowledge)

        return eventIdsToAcknowledge.isNotEmpty()
    }

    private fun processEmailLabelChanged(events: List<Event>): Boolean {
        val isEmailLabelChangedEvent: (Event) -> Boolean = { it.cmd == Event.Cmd.peerEmailChangedLabels }
        val toIdAndMetadataPair: (Event) -> Pair<Long, PeerEmailLabelsChangedStatusUpdate> =
                { Pair( it.rowid, PeerEmailLabelsChangedStatusUpdate.fromJSON(it.params)) }
        val emailLabelChangedSuccessfully: (Pair<Long, PeerEmailLabelsChangedStatusUpdate>) -> Boolean =
                { (_, metadata) ->
                    try {
                        updateEmailLabelChangedStatus(metadata)
                        // insertion success, try to acknowledge it
                        true
                    }catch (ex: Exception) {
                        false
                    }
                }
        val toEventId: (Pair<Long, PeerEmailLabelsChangedStatusUpdate>) -> Long =
                { (eventId, _) -> eventId }

        val eventIdsToAcknowledge = events
                .filter(isEmailLabelChangedEvent)
                .map(toIdAndMetadataPair)
                .filter(emailLabelChangedSuccessfully)
                .map(toEventId)

        if (eventIdsToAcknowledge.isNotEmpty() && acknoledgeEvents)
            eventsToAcknowldege.addAll(eventIdsToAcknowledge)

        return eventIdsToAcknowledge.isNotEmpty()
    }

    private fun processThreadLabelChanged(events: List<Event>): Boolean {
        val isThreadLabelChangedEvent: (Event) -> Boolean = { it.cmd == Event.Cmd.peerThreadChangedLabels }
        val toIdAndMetadataPair: (Event) -> Pair<Long, PeerThreadLabelsChangedStatusUpdate> =
                { Pair( it.rowid, PeerThreadLabelsChangedStatusUpdate.fromJSON(it.params)) }
        val emailLabelChangedSuccessfully: (Pair<Long, PeerThreadLabelsChangedStatusUpdate>) -> Boolean =
                { (_, metadata) ->
                    try {
                        updateThreadLabelChangedStatus(metadata)
                        // insertion success, try to acknowledge it
                        true
                    }catch (ex: Exception) {
                        false
                    }
                }
        val toEventId: (Pair<Long, PeerThreadLabelsChangedStatusUpdate>) -> Long =
                { (eventId, _) -> eventId }

        val eventIdsToAcknowledge = events
                .filter(isThreadLabelChangedEvent)
                .map(toIdAndMetadataPair)
                .filter(emailLabelChangedSuccessfully)
                .map(toEventId)

        if (eventIdsToAcknowledge.isNotEmpty() && acknoledgeEvents)
            eventsToAcknowldege.addAll(eventIdsToAcknowledge)

        return eventIdsToAcknowledge.isNotEmpty()
    }

    private fun processEmailDeletedPermanently(events: List<Event>): Boolean {
        val isEmailDeletedPermanentlyEvent: (Event) -> Boolean = { it.cmd == Event.Cmd.peerEmailDeleted }
        val toIdAndMetadataPair: (Event) -> Pair<Long, PeerEmailDeletedStatusUpdate> =
                { Pair( it.rowid, PeerEmailDeletedStatusUpdate.fromJSON(it.params)) }
        val emailDeletedSuccessfully: (Pair<Long, PeerEmailDeletedStatusUpdate>) -> Boolean =
                { (_, metadata) ->
                    try {
                        updateEmailDeletedPermanentlyStatus(metadata)
                        // insertion success, try to acknowledge it
                        true
                    }catch (ex: Exception) {
                        false
                    }
                }
        val toEventId: (Pair<Long, PeerEmailDeletedStatusUpdate>) -> Long =
                { (eventId, _) -> eventId }

        val eventIdsToAcknowledge = events
                .filter(isEmailDeletedPermanentlyEvent)
                .map(toIdAndMetadataPair)
                .filter(emailDeletedSuccessfully)
                .map(toEventId)

        if (eventIdsToAcknowledge.isNotEmpty() && acknoledgeEvents)
            eventsToAcknowldege.addAll(eventIdsToAcknowledge)

        return eventIdsToAcknowledge.isNotEmpty()
    }

    private fun processThreadDeletedPermanently(events: List<Event>): Boolean {
        val isThreadDeletedPermanentlyEvent: (Event) -> Boolean = { it.cmd == Event.Cmd.peerThreadDeleted }
        val toIdAndMetadataPair: (Event) -> Pair<Long, PeerThreadDeletedStatusUpdate> =
                { Pair( it.rowid, PeerThreadDeletedStatusUpdate.fromJSON(it.params)) }
        val threadDeletedSuccessfully: (Pair<Long, PeerThreadDeletedStatusUpdate>) -> Boolean =
                { (_, metadata) ->
                    try {
                        updateThreadDeletedPermanentlyStatus(metadata)
                        // insertion success, try to acknowledge it
                        true
                    }catch (ex: Exception) {
                        false
                    }
                }
        val toEventId: (Pair<Long, PeerThreadDeletedStatusUpdate>) -> Long =
                { (eventId, _) -> eventId }

        val eventIdsToAcknowledge = events
                .filter(isThreadDeletedPermanentlyEvent)
                .map(toIdAndMetadataPair)
                .filter(threadDeletedSuccessfully)
                .map(toEventId)

        if (eventIdsToAcknowledge.isNotEmpty() && acknoledgeEvents)
            eventsToAcknowldege.addAll(eventIdsToAcknowledge)

        return eventIdsToAcknowledge.isNotEmpty()
    }

    private fun processLabelCreated(events: List<Event>): Boolean {
        val isLabelCreatedEvent: (Event) -> Boolean = { it.cmd == Event.Cmd.peerLabelCreated }
        val toIdAndMetadataPair: (Event) -> Pair<Long, PeerLabelCreatedStatusUpdate> =
                { Pair( it.rowid, PeerLabelCreatedStatusUpdate.fromJSON(it.params)) }
        val labelCreatedSuccessfully: (Pair<Long, PeerLabelCreatedStatusUpdate>) -> Boolean =
                { (_, metadata) ->
                    try {
                        updateLabelCreatedStatus(metadata)
                        // insertion success, try to acknowledge it
                        true
                    }catch (ex: Exception) {
                        false
                    }
                }
        val toEventId: (Pair<Long, PeerLabelCreatedStatusUpdate>) -> Long =
                { (eventId, _) -> eventId }

        val eventIdsToAcknowledge = events
                .filter(isLabelCreatedEvent)
                .map(toIdAndMetadataPair)
                .filter(labelCreatedSuccessfully)
                .map(toEventId)

        if (eventIdsToAcknowledge.isNotEmpty() && acknoledgeEvents)
            eventsToAcknowldege.addAll(eventIdsToAcknowledge)

        return eventIdsToAcknowledge.isNotEmpty()
    }

    private fun processTrackingUpdates(events: List<Event>): Boolean {
        val isTrackingUpdateEvent: (Event) -> Boolean = { it.cmd == Event.Cmd.trackingUpdate }
        val isAnEventGeneratedByMe: (Pair<Long, TrackingUpdate>) -> Boolean = { it.second.from != activeAccount.recipientId }
        val toIdAndTrackingUpdatePair: (Event) -> Pair<Long, TrackingUpdate> = {
            Pair(it.rowid, TrackingUpdate.fromJSON(it.params))
        }

        val trackingUpdatesPair = events.filter(isTrackingUpdateEvent)
                .map(toIdAndTrackingUpdatePair).filter(isAnEventGeneratedByMe)
        val eventIdsToAcknowledge = trackingUpdatesPair.map { it.first }
        val trackingUpdates = trackingUpdatesPair.map { it.second }

        createFeedItems(trackingUpdates)
        changeDeliveryTypes(trackingUpdates)
        if(eventIdsToAcknowledge.isNotEmpty() && acknoledgeEvents)
            eventsToAcknowldege.addAll(events.filter(isTrackingUpdateEvent)
                    .map(toIdAndTrackingUpdatePair).map { it.first })

        return eventIdsToAcknowledge.isNotEmpty()
    }

    private fun processOnError(events: List<Event>): Boolean {


        val eventIdsToAcknowledge = events.filter { it.cmd == Event.Cmd.newError }
                .map { it.rowid }

        if (eventIdsToAcknowledge.isNotEmpty())
            eventsToAcknowldege.addAll(eventIdsToAcknowledge)

        return eventIdsToAcknowledge.isNotEmpty()
    }



    private fun reloadMailbox(shouldReload: Boolean): List<EmailPreview> {
        return if (shouldReload)
            db.getThreadsFromMailboxLabel(
                    labelName = label.text,
                    startDate = null,
                    limit = Math.max(20, loadedThreadsCount?:30),
                    rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(label),
                    userEmail = activeAccount.userEmail)
                    .map { EmailPreview.fromEmailThread(it) }
        else throw EventHelper.NothingNewException()
    }

    private fun insertIncomingEmailTransaction(metadata: EmailMetadata) =
            db.insertIncomingEmail(signalClient, emailInsertionApiClient, metadata, activeAccount)

    private fun updateThreadReadStatus(metadata: PeerReadThreadStatusUpdate) =
            db.updateUnreadStatusByThreadId(metadata.threadIds, metadata.unread)

    private fun updateEmailReadStatus(metadata: PeerReadEmailStatusUpdate) =
            db.updateUnreadStatusByMetadataKeys(metadata.metadataKeys, metadata.unread)

    private fun updateUnsendEmailStatus(metadata: PeerUnsendEmailStatusUpdate) =
            db.updateUnsendStatusByMetadataKey(metadata.metadataKey, metadata.unsendDate)


    private fun updateUsernameStatus(metadata: PeerUsernameChangedStatusUpdate) =
            db.updateUserName(activeAccount.recipientId, metadata.name)

    private fun updateEmailLabelChangedStatus(metadata: PeerEmailLabelsChangedStatusUpdate) =
            db.updateEmailLabels(metadata.metadataKeys, metadata.labelsAdded, metadata.labelsRemoved)

    private fun updateThreadLabelChangedStatus(metadata: PeerThreadLabelsChangedStatusUpdate) =
            db.updateThreadLabels(metadata.threadIds, metadata.labelsAdded, metadata.labelsRemoved)

    private fun updateEmailDeletedPermanentlyStatus(metadata: PeerEmailDeletedStatusUpdate) =
            db.updateDeleteEmailPermanently(metadata.metadataKeys)

    private fun updateThreadDeletedPermanentlyStatus(metadata: PeerThreadDeletedStatusUpdate) =
            db.updateDeleteThreadPermanently(metadata.threadIds)

    private fun updateLabelCreatedStatus(metadata: PeerLabelCreatedStatusUpdate) =
            db.updateCreateLabel(metadata.text, metadata.color)


    private fun updateExistingEmailTransaction(metadata: EmailMetadata) =
            db.updateExistingEmail(metadata, activeAccount)


    private fun changeDeliveryTypeByMetadataKey(metadataKeys: List<Long>, deliveryType: DeliveryTypes) =
            db.updateDeliveryTypeByMetadataKey(metadataKeys, deliveryType)

    private fun createFeedItems(trackingUpdates: List<TrackingUpdate>) =
            db.updateFeedItems(trackingUpdates)


    private fun acknowledgeEventsIgnoringErrors(eventIdsToAcknowledge: List<Long>): Boolean {
        try {
            if(eventIdsToAcknowledge.isNotEmpty())
                mailboxAPIClient.acknowledgeEvents(eventIdsToAcknowledge)
        } catch (ex: IOException) {
            // if this request fails, just ignore it, we can acknowledge again later
        }
        return eventIdsToAcknowledge.isNotEmpty()
    }



    private fun changeDeliveryTypes(trackingUpdates: List<TrackingUpdate>){
        changeDeliveryTypeByMetadataKey(
                metadataKeys = trackingUpdates.filter { it.type == DeliveryTypes.DELIVERED }.map { it.metadataKey },
                deliveryType = DeliveryTypes.DELIVERED)
        changeDeliveryTypeByMetadataKey(
                metadataKeys = trackingUpdates.filter { it.type == DeliveryTypes.READ }.map { it.metadataKey },
                deliveryType = DeliveryTypes.READ)
        changeDeliveryTypeByMetadataKey(
                metadataKeys = trackingUpdates.filter { it.type == DeliveryTypes.UNSEND }.map { it.metadataKey },
                deliveryType = DeliveryTypes.UNSEND)
    }

    class NothingNewException: Exception()
}