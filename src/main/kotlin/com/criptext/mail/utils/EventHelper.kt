package com.criptext.mail.utils

import com.criptext.mail.api.EmailInsertionAPIClient
import com.criptext.mail.api.Hosts
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.*
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.db.models.signal.CRPreKey
import com.criptext.mail.db.models.signal.CRSignedPreKey
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.mailbox.data.MailboxAPIClient
import com.criptext.mail.scenes.mailbox.data.UpdateBannerData
import com.criptext.mail.scenes.mailbox.data.UpdateBannerEventData
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalKeyGenerator
import com.criptext.mail.utils.file.FileUtils
import com.github.kittinunf.result.Result
import com.squareup.picasso.Picasso
import org.json.JSONObject
import org.whispersystems.libsignal.DuplicateMessageException
import java.io.File
import java.io.IOException
import java.util.*

class EventHelper(private val db: EventLocalDB,
                  httpClient: HttpClient,
                  private val storage: KeyValueStorage,
                  private val activeAccount: ActiveAccount,
                  private val signalClient: SignalClient,
                  private val acknoledgeEvents: Boolean){

    private val mailboxAPIClient = MailboxAPIClient(httpClient, activeAccount.jwt)
    private val emailInsertionApiClient = EmailInsertionAPIClient(httpClient, activeAccount.jwt)
    private val eventsToAcknowldege = mutableListOf<Long>()

    private val newsHttpClient = HttpClient.Default(Hosts.newsRepository, HttpClient.AuthScheme.jwt,
            14000L, 7000L)

    private val newsClient = MailboxAPIClient(newsHttpClient, activeAccount.jwt)

    private lateinit var label: Label
    private var loadedThreadsCount: Int? = null
    private var updateBannerData: UpdateBannerData? = null
    private val linkDevicesEvents: MutableList<DeviceInfo?> = mutableListOf()
    private var shouldCallAgain = false
    private var shouldNotify = false

    fun setupForMailbox(label: Label, threadCount: Int?){
        this.label = label
        loadedThreadsCount = threadCount
    }

    val processEvents: (Pair<List<Event>, Boolean>) -> Result<EventHelperResultData, Exception> = { events ->
        Result.of {

            val shouldReload = processLowPreKeys(events.first).or(processNewEmails(events.first)).or(processTrackingUpdates(events.first))
                    .or(processThreadReadStatusChanged(events.first)).or(processUnsendEmailStatusChanged(events.first))
                    .or(processPeerUsernameChanged(events.first)).or(processEmailLabelChanged(events.first))
                    .or(processLabelCreated(events.first)).or(processThreadLabelChanged(events.first))
                    .or(processEmailDeletedPermanently(events.first)).or(processThreadDeletedPermanently(events.first))
                    .or(processOnError(events.first)).or(processEmailReadStatusChanged(events.first)).or(processUpdateBannerData(events.first))
                    .or(processLinkRequestEvents(events.first)).or(processSyncRequestEvents(events.first)).or(processProfilePicChangePeer(events.first))
            EventHelperResultData(reloadMailbox(shouldReload.or(acknowledgeEventsIgnoringErrors(eventsToAcknowldege))),
                    updateBannerData, linkDevicesEvents, shouldNotify)
        }
    }

    private fun processProfilePicChangePeer(events: List<Event>): Boolean {
        val isLowOnPreKeysEvent: (Event) -> Boolean = { it.cmd == Event.Cmd.profilePictureChanged }
        val toEventIds: (Event) -> Long =
                { it.rowid }

        val eventIdsToAcknowledge = events
                .filter(isLowOnPreKeysEvent)
                .map(toEventIds)

        if(eventIdsToAcknowledge.isNotEmpty()){

            Picasso.get().invalidate(Hosts.restApiBaseUrl.plus("/user/avatar/${activeAccount.recipientId}"))

            val cache = File(db.getCacheDir(), "picasso-cache")
            if (cache.exists() && cache.isDirectory) {
                FileUtils.deleteDir(cache)
            }
            if (acknoledgeEvents)
                eventsToAcknowldege.addAll(eventIdsToAcknowledge)
        }

        return eventIdsToAcknowledge.isNotEmpty()
    }

    private fun processLowPreKeys(events: List<Event>): Boolean {
        val isLowOnPreKeysEvent: (Event) -> Boolean = { it.cmd == Event.Cmd.lowOnPreKeys }
        val toEventIds: (Event) -> Long =
                { it.rowid }

        val eventIdsToAcknowledge = events
                .filter(isLowOnPreKeysEvent)
                .map(toEventIds)

        if(eventIdsToAcknowledge.isNotEmpty()){
            val keyGenerator = SignalKeyGenerator.Default(DeviceUtils.getDeviceType())
            val remainingKeys = db.getAllPreKeys(activeAccount.id).map { it.preKeyId }
            val registrationBundles = keyGenerator.register(activeAccount.recipientId,
                    activeAccount.deviceId)


            val response = Result.of {
                mailboxAPIClient.insertPreKeys(
                        preKeys = registrationBundles.uploadBundle.preKeys,
                        excludedKeys = remainingKeys)
            }
            if(response is Result.Success){
                val preKeyList = registrationBundles.privateBundle.preKeys.entries.map { (key, value) ->
                    CRPreKey(id = 0, preKeyId = key, byteString = value, accountId = activeAccount.id)
                }.filter { it.preKeyId !in remainingKeys }
                db.insertPreKeys(preKeyList)

                if (acknoledgeEvents)
                    acknowledgeEventsIgnoringErrors(eventIdsToAcknowledge.map { it })
            }
        }

        return eventIdsToAcknowledge.isNotEmpty()
    }

    private fun processLinkRequestEvents(events: List<Event>): Boolean {
        val isDeviceLinkRequest: (Event) -> Boolean = { it.cmd == Event.Cmd.deviceAuthRequest }
        val toIdAndDeviceInfoPair: (Event) -> Pair<Long, DeviceInfo.UntrustedDeviceInfo> =
                { Pair( it.rowid, DeviceInfo.UntrustedDeviceInfo.fromJSON(it.params)) }

        val eventIdsToAcknowledge = events
                .filter(isDeviceLinkRequest)
                .map(toIdAndDeviceInfoPair)

        if (eventIdsToAcknowledge.isNotEmpty() && acknoledgeEvents)
            acknowledgeEventsIgnoringErrors(eventIdsToAcknowledge.map { it.first })

        val deviceInfo = eventIdsToAcknowledge.map { it.second }
        linkDevicesEvents.add(if(deviceInfo.isEmpty())
            null
                else{
                shouldNotify = true
                eventIdsToAcknowledge.map { it.second }.last()
            }
        )

        return eventIdsToAcknowledge.isNotEmpty()
    }

    private fun processSyncRequestEvents(events: List<Event>): Boolean {
        val isDeviceLinkRequest: (Event) -> Boolean = { it.cmd == Event.Cmd.syncBeginRequest }
        val toIdAndDeviceInfoPair: (Event) -> Pair<Long, DeviceInfo.TrustedDeviceInfo> =
                { Pair( it.rowid, DeviceInfo.TrustedDeviceInfo.fromJSON(it.params)) }

        val eventIdsToAcknowledge = events
                .filter(isDeviceLinkRequest)
                .map(toIdAndDeviceInfoPair)

        if (eventIdsToAcknowledge.isNotEmpty() && acknoledgeEvents)
            acknowledgeEventsIgnoringErrors(eventIdsToAcknowledge.map { it.first })

        val deviceInfo = eventIdsToAcknowledge.map { it.second }
        linkDevicesEvents.add(if(deviceInfo.isEmpty())
            null
                else {
                    shouldNotify = true
                    eventIdsToAcknowledge.map { it.second }.last()
                }
        )

        return eventIdsToAcknowledge.isNotEmpty()
    }

    private fun processUpdateBannerData(events: List<Event>): Boolean {
        val isUpdateBannerEvent: (Event) -> Boolean = { it.cmd == Event.Cmd.updateBannerEvent }
        val toIdAndMetadataPair: (Event) -> Pair<Long, UpdateBannerEventData> =
                { Pair( it.rowid, UpdateBannerEventData.fromJSON(it.params)) }
        val updateBannerDataList = mutableListOf<UpdateBannerData>()
        val getBannerDataSuccessfully: (Pair<Long, UpdateBannerEventData>) -> Boolean =
                { (_, updateEventData) ->
                    val operation = getImageFromCdn(updateEventData)
                    when(operation){
                        is Result.Success ->{
                            updateBannerDataList.add(operation.value)
                            shouldNotify = true
                            true
                        }
                        else -> false
                    }
                }
        val toEventId: (Pair<Long, UpdateBannerEventData>) -> Long =
                { (eventId, _) -> eventId }

        val eventIdsToAcknowledge = events
                .filter(isUpdateBannerEvent)
                .map(toIdAndMetadataPair)
                .filter(getBannerDataSuccessfully)
                .map(toEventId)

        if (eventIdsToAcknowledge.isNotEmpty() && acknoledgeEvents)
            eventsToAcknowldege.addAll(eventIdsToAcknowledge)

        updateBannerData = if(updateBannerDataList.isEmpty()) null else updateBannerDataList.last()
        return eventIdsToAcknowledge.isNotEmpty()
    }


    private fun processNewEmails(events: List<Event>): Boolean {
        val isNewEmailEvent: (Event) -> Boolean = { it.cmd == Event.Cmd.newEmail }
        val toIdAndMetadataPair: (Event) -> Pair<Long, EmailMetadata> =
                { Pair( it.rowid, EmailMetadata.fromJSON(it.params)) }
        val emailInsertedSuccessfully: (Pair<Long, EmailMetadata>) -> Boolean =
                { (_, metadata) ->
                    try {
                        insertIncomingEmailTransaction(metadata)
                        shouldNotify = true
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
        val threadReadStatusChangedSuccessfully: (Pair<Long, PeerReadThreadStatusUpdate>) -> Boolean =
                { (_, metadata) ->
                    try {
                        updateThreadReadStatus(metadata)
                        // insertion success, try to acknowledge it
                        true
                    }
                    catch (ex: Exception) {
                        true
                    }
                }
        val toEventId: (Pair<Long, PeerReadThreadStatusUpdate>) -> Long =
                { (eventId, _) -> eventId }

        val eventIdsToAcknowledge = events
                .filter(isThreadReadStatusChangedEvent)
                .map(toIdAndMetadataPair)
                .filter(threadReadStatusChangedSuccessfully)
                .map(toEventId)

        if (eventIdsToAcknowledge.isNotEmpty() && acknoledgeEvents)
            eventsToAcknowldege.addAll(eventIdsToAcknowledge)

        return eventIdsToAcknowledge.isNotEmpty()
    }

    private fun processEmailReadStatusChanged(events: List<Event>): Boolean {
        val isEmailReadStatusChangedEvent: (Event) -> Boolean = { it.cmd == Event.Cmd.peerEmailReadStatusUpdate }
        val toIdAndMetadataPair: (Event) -> Pair<Long, PeerReadEmailStatusUpdate> =
                { Pair( it.rowid, PeerReadEmailStatusUpdate.fromJSON(it.params)) }
        val emailReadSuccessfully: (Pair<Long, PeerReadEmailStatusUpdate>) -> Boolean =
                { (_, metadata) ->
                    try {
                        updateEmailReadStatus(metadata)
                        // insertion success, try to acknowledge it
                        true
                    }
                    catch (ex: Exception) {
                        true
                    }
                }
        val toEventId: (Pair<Long, PeerReadEmailStatusUpdate>) -> Long =
                { (eventId, _) -> eventId }

        val eventIdsToAcknowledge = events
                .filter(isEmailReadStatusChangedEvent)
                .map(toIdAndMetadataPair)
                .filter(emailReadSuccessfully)
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
                        true
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
                        true
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
                        true
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
                        true
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
                        true
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
        val toIdAndTrackingUpdatePair: (Event) -> Pair<Long, TrackingUpdate> = {
            Pair(it.rowid, TrackingUpdate.fromJSON(it.params))
        }

        val trackingUpdatesPair = events.filter(isTrackingUpdateEvent)
                .map(toIdAndTrackingUpdatePair)
        val eventIdsToAcknowledge = trackingUpdatesPair.map { it.first }
        val trackingUpdates = trackingUpdatesPair.map { it.second }

        createFeedItems(trackingUpdates)
        changeDeliveryTypes(trackingUpdates)
        trackingUpdates.forEach {
            if(it.type == DeliveryTypes.UNSEND)
                updateUnsendEmailStatus(PeerUnsendEmailStatusUpdate(it.metadataKey, it.date))
        }
        if(eventIdsToAcknowledge.isNotEmpty() && acknoledgeEvents)
            eventsToAcknowldege.addAll(eventIdsToAcknowledge)

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
                    userEmail = activeAccount.userEmail,
                    activeAccount = activeAccount)
                    .map { EmailPreview.fromEmailThread(it) }
        else throw EventHelper.NothingNewException()
    }

    private fun getImageFromCdn(metadata: UpdateBannerEventData): Result<UpdateBannerData, java.lang.Exception> {
        return Result.of {
            UpdateBannerData.fromJSON(newsClient.getUpdateBannerData(
                    metadata.messageCode,
                    Locale.getDefault().toString().toLowerCase()).body
            ).copy(version = metadata.version, operator = metadata.operator)
        }
    }

    private fun insertIncomingEmailTransaction(metadata: EmailMetadata) =
            db.insertIncomingEmail(signalClient, emailInsertionApiClient, metadata, activeAccount)

    private fun updateThreadReadStatus(metadata: PeerReadThreadStatusUpdate) =
            db.updateUnreadStatusByThreadId(metadata.threadIds, metadata.unread, activeAccount.id)

    private fun updateEmailReadStatus(metadata: PeerReadEmailStatusUpdate) =
            db.updateUnreadStatusByMetadataKeys(metadata.metadataKeys, metadata.unread, activeAccount.id)

    private fun updateUnsendEmailStatus(metadata: PeerUnsendEmailStatusUpdate) =
            db.updateUnsendStatusByMetadataKey(metadata.metadataKey, metadata.unsendDate, activeAccount)


    private fun updateUsernameStatus(metadata: PeerUsernameChangedStatusUpdate) {
        activeAccount.updateFullName(storage, metadata.name)
        db.updateUserName(activeAccount.recipientId, metadata.name, activeAccount.id)
    }

    private fun updateEmailLabelChangedStatus(metadata: PeerEmailLabelsChangedStatusUpdate) =
            db.updateEmailLabels(metadata.metadataKeys, metadata.labelsAdded, metadata.labelsRemoved, activeAccount.id)

    private fun updateThreadLabelChangedStatus(metadata: PeerThreadLabelsChangedStatusUpdate) =
            db.updateThreadLabels(metadata.threadIds, metadata.labelsAdded, metadata.labelsRemoved, activeAccount.id)

    private fun updateEmailDeletedPermanentlyStatus(metadata: PeerEmailDeletedStatusUpdate) =
            db.updateDeleteEmailPermanently(metadata.metadataKeys, activeAccount)

    private fun updateThreadDeletedPermanentlyStatus(metadata: PeerThreadDeletedStatusUpdate) =
            db.updateDeleteThreadPermanently(metadata.threadIds, activeAccount)

    private fun updateLabelCreatedStatus(metadata: PeerLabelCreatedStatusUpdate) =
            db.updateCreateLabel(metadata.text, metadata.color, metadata.uuid, activeAccount.id)


    private fun updateExistingEmailTransaction(metadata: EmailMetadata) =
            db.updateExistingEmail(metadata, activeAccount)


    private fun changeDeliveryTypeByMetadataKey(metadataKeys: List<Long>, deliveryType: DeliveryTypes) =
            db.updateDeliveryTypeByMetadataKey(metadataKeys, deliveryType, activeAccount.id)

    private fun createFeedItems(trackingUpdates: List<TrackingUpdate>) =
            db.updateFeedItems(trackingUpdates, activeAccount.id)


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