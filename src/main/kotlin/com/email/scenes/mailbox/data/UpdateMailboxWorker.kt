package com.email.scenes.mailbox.data

import com.email.R
import com.email.api.EmailInsertionAPIClient
import com.email.api.HttpClient
import com.email.api.HttpErrorHandlingHelper
import com.email.api.models.*
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.*
import com.email.db.dao.*
import com.email.db.models.*
import com.email.email_preview.EmailPreview
import com.email.scenes.label_chooser.SelectedLabels
import com.email.scenes.label_chooser.data.LabelWrapper
import com.email.signal.SignalClient
import com.email.utils.ColorUtils
import com.email.utils.DateUtils
import com.email.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONArray
import org.whispersystems.libsignal.DuplicateMessageException
import java.io.IOException
import java.util.*

/**
 * Created by sebas on 3/22/18.
 */

class UpdateMailboxWorker(
        private val signalClient: SignalClient,
        private val db: MailboxLocalDB,
        private val emailDao: EmailDao,
        private val emailLabelDao: EmailLabelDao,
        private val labelDao: LabelDao,
        private val contactDao: ContactDao,
        private val fileDao: FileDao,
        private val feedItemDao: FeedItemDao,
        private val dao: EmailInsertionDao,
        private val accountDao: AccountDao,
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
                    labelName = label.text,
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

    private fun updateThreadReadStatus(metadata: PeerReadThreadStatusUpdate) =
            emailDao.toggleReadByThreadId(metadata.threadIds, metadata.unread)

    private fun updateUnsendEmailStatus(metadata: PeerUnsendEmailStatusUpdate) {
        emailDao.changeDeliveryTypeByMetadataKey(metadata.metadataKey, DeliveryTypes.UNSEND)
        emailDao.unsendEmailByMetadataKey(metadata.metadataKey, "", "",
                DateUtils.getDateFromString(metadata.unsendDate, null))
        fileDao.changeFileStatusByEmailid(emailDao.getEmailByMetadataKey(metadata.metadataKey).id, 0)
    }

    private fun updateUsernameStatus(metadata: PeerUsernameChangedStatusUpdate) {
        contactDao.updateContactName("${activeAccount.recipientId}@${Contact.mainDomain}", metadata.name)
        accountDao.updateProfileName(metadata.name, activeAccount.recipientId)
    }

    private fun updateEmailLabelChangedStatus(metadata: PeerEmailLabelsChangedStatusUpdate) {
        if(!metadata.metadataKeys.isEmpty()){

            val emailIds = emailDao.getAllEmailsByMetadataKey(metadata.metadataKeys).map { it.id }
            val removedLabelIds = labelDao.get(metadata.labelsRemoved).map { it.id }
            val addedLabelIds = labelDao.get(metadata.labelsAdded)

            emailLabelDao.deleteRelationByLabelsAndEmailIds(removedLabelIds, emailIds)


            val selectedLabels = SelectedLabels()
            val labelsWrapper = addedLabelIds.map { LabelWrapper(it) }
            selectedLabels.addMultipleSelected(labelsWrapper)
            val labelEmails = emailIds.flatMap{ emailId ->
                selectedLabels.toIDs().map{ labelId ->
                    EmailLabel(emailId = emailId, labelId = labelId)
                }
            }
            emailLabelDao.insertAll(labelEmails)
        }
    }

    private fun updateThreadLabelChangedStatus(metadata: PeerThreadLabelsChangedStatusUpdate) {
        if(!metadata.threadIds.isEmpty()){

            val emailIds = emailDao.getEmailsFromThreadIds(metadata.threadIds).map { it.id }
            val removedLabelIds = labelDao.get(metadata.labelsRemoved).map { it.id }
            val addedLabelIds = labelDao.get(metadata.labelsAdded)

            emailLabelDao.deleteRelationByLabelsAndEmailIds(removedLabelIds, emailIds)


            val selectedLabels = SelectedLabels()
            val labelsWrapper = addedLabelIds.map { LabelWrapper(it) }
            selectedLabels.addMultipleSelected(labelsWrapper)
            val labelEmails = emailIds.flatMap{ emailId ->
                selectedLabels.toIDs().map{ labelId ->
                    EmailLabel(emailId = emailId, labelId = labelId)
                }
            }
            emailLabelDao.insertAll(labelEmails)
        }
    }

    private fun updateEmailDeletedPermanentlyStatus(metadata: PeerEmailDeletedStatusUpdate) {
        if(!metadata.metadataKeys.isEmpty()){
            emailDao.deleteAll(emailDao.getAllEmailsByMetadataKey(metadata.metadataKeys))
        }
    }

    private fun updateThreadDeletedPermanentlyStatus(metadata: PeerThreadDeletedStatusUpdate) {
        if(!metadata.threadIds.isEmpty()){
            emailDao.deleteThreads(metadata.threadIds)
        }
    }

    private fun updateLabelCreatedStatus(metadata: PeerLabelCreatedStatusUpdate) = labelDao.insert(Label(
                id = 0,
                text = metadata.text,
                color = ColorUtils.colorStringByName(metadata.color),
                visible = true,
                type = LabelTypes.CUSTOM
        ))


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

    private fun changeDeliveryTypeByMetadataKey(metadataKeys: List<Long>, deliveryType: DeliveryTypes) {
        if (metadataKeys.isNotEmpty()) {
            emailDao.changeDeliveryTypeByMetadataKey(metadataKeys, deliveryType)
        }
    }

    private fun createFeedItems(trackingUpdates: List<TrackingUpdate>){

        val feeds = mutableListOf<FeedItem>()
        trackingUpdates.forEach {
            val existingEmail = emailDao.findEmailByMetadataKey(it.metadataKey)
            if(existingEmail != null && it.type == DeliveryTypes.READ){
                feeds.add(FeedItem(
                        id = 0,
                        date = Date(),
                        feedType = FeedType.OPEN_EMAIL,
                        location = "",
                        seen = false,
                        emailId = existingEmail.id,
                        contactId = contactDao.getContact("${it.from}@${Contact.mainDomain}")!!.id,
                        fileId = null
                ))
            }
        }

        feedItemDao.insertFeedItems(feeds)
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
        acknowledgeEventsIgnoringErrors(eventIdsToAcknowledge)

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

        if (eventIdsToAcknowledge.isNotEmpty())
            acknowledgeEventsIgnoringErrors(eventIdsToAcknowledge)

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

        if (eventIdsToAcknowledge.isNotEmpty())
            acknowledgeEventsIgnoringErrors(eventIdsToAcknowledge)

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

        if (eventIdsToAcknowledge.isNotEmpty())
            acknowledgeEventsIgnoringErrors(eventIdsToAcknowledge)

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

        if (eventIdsToAcknowledge.isNotEmpty())
            acknowledgeEventsIgnoringErrors(eventIdsToAcknowledge)

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

        if (eventIdsToAcknowledge.isNotEmpty())
            acknowledgeEventsIgnoringErrors(eventIdsToAcknowledge)

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

        if (eventIdsToAcknowledge.isNotEmpty())
            acknowledgeEventsIgnoringErrors(eventIdsToAcknowledge)

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

        if (eventIdsToAcknowledge.isNotEmpty())
            acknowledgeEventsIgnoringErrors(eventIdsToAcknowledge)

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

        if (eventIdsToAcknowledge.isNotEmpty())
            acknowledgeEventsIgnoringErrors(eventIdsToAcknowledge)

        return eventIdsToAcknowledge.isNotEmpty()
    }


    private val processEvents: (List<Event>) -> Result<List<EmailPreview>, Exception> = { events ->
        Result.of {
            val shouldReload = processTrackingUpdates(events)
                            || processNewEmails(events) || processThreadReadStatusChanged(events) ||
                    processUnsendEmailStatusChanged(events) || processPeerUsernameChanged(events) ||
                    processEmailLabelChanged(events) || processThreadLabelChanged(events) ||
                    processEmailDeletedPermanently(events) || processThreadDeletedPermanently(events) ||
                    processLabelCreated(events)
            reloadMailbox(shouldReload)
        }
    }

    private class NothingNewException: Exception()
}
