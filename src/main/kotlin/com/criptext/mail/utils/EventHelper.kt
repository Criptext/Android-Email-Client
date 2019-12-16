package com.criptext.mail.utils

import com.criptext.mail.api.EmailInsertionAPIClient
import com.criptext.mail.api.Hosts
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.*
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
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
import com.criptext.mail.utils.peerdata.PeerDeleteLabelData
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
    private var updateBannerData: UpdateBannerData? = null
    private val linkDevicesEvents: MutableList<DeviceInfo?> = mutableListOf()
    private var shouldNotify = false
    private var newEmails = mutableListOf<EmailPreview>()
    private var customLabels = mutableListOf<Label>()
    private var threadReads = mutableListOf<Pair<List<String>, Boolean>>()
    private var emailReads = mutableListOf<Pair<List<Long>, Boolean>>()
    private var movedThread = mutableListOf<Triple<List<String>, List<Label>?, List<Label>?>>()
    private var movedEmail = mutableListOf<Triple<List<Long>, List<Label>?, List<Label>?>>()
    private var nameChanged: String = ""
    private var unsend: Pair<Long, Date>? = null


    fun setupForMailbox(label: Label){
        this.label = label
    }

    val processEvents: (Pair<List<Event>, Boolean>) -> Result<EventHelperResultData, Exception> = { events ->
        Result.of {
            val eventList = events.first
            eventList.forEach {
                when(it.cmd){
                    Event.Cmd.profilePictureChanged -> processProfilePicChangePeer(it)
                    Event.Cmd.lowOnPreKeys -> processLowPreKeys(it)
                    Event.Cmd.deviceAuthRequest -> processLinkRequestEvents(it)
                    Event.Cmd.syncBeginRequest -> processSyncRequestEvents(it)
                    Event.Cmd.updateBannerEvent -> processUpdateBannerData(it)
                    Event.Cmd.newEmail -> processNewEmails(it)
                    Event.Cmd.peerEmailThreadReadStatusUpdate -> processThreadReadStatusChanged(it)
                    Event.Cmd.peerEmailReadStatusUpdate -> processEmailReadStatusChanged(it)
                    Event.Cmd.peerEmailUnsendStatusUpdate -> processUnsendEmailStatusChanged(it)
                    Event.Cmd.peerUserChangeName -> processPeerUsernameChanged(it)
                    Event.Cmd.peerEmailChangedLabels -> processEmailLabelChanged(it)
                    Event.Cmd.peerThreadChangedLabels -> processThreadLabelChanged(it)
                    Event.Cmd.peerEmailDeleted -> processEmailDeletedPermanently(it)
                    Event.Cmd.peerThreadDeleted -> processThreadDeletedPermanently(it)
                    Event.Cmd.peerLabelCreated -> processLabelCreated(it)
                    Event.Cmd.peerLabelEdited -> processLabelEdited(it)
                    Event.Cmd.peerLabelDeleted -> processLabelDeleted(it)
                    Event.Cmd.trackingUpdate -> processTrackingUpdates(it)
                    Event.Cmd.newError -> processOnError(it)
                }
            }

            acknowledgeEventsIgnoringErrors(eventsToAcknowldege)
            EventHelperResultData(updateBannerData, linkDevicesEvents, shouldNotify,
                    newEmails, customLabels, threadReads, emailReads, movedThread, movedEmail,
                    nameChanged, unsend)
        }
    }

    private fun processProfilePicChangePeer(event: Event) {
        val operation = Result.of {
            UIUtils.checkForCacheCleaning(storage, db.getCacheDir(), activeAccount)
        }
        when(operation){
            is Result.Success -> {
                if (acknoledgeEvents)
                    eventsToAcknowldege.add(event.rowid)
            }
        }
    }

    private fun processLowPreKeys(event: Event) {
        val keyGenerator = SignalKeyGenerator.Default(DeviceUtils.getDeviceType())
        val remainingKeys = db.getAllPreKeys(activeAccount.id).map { it.preKeyId }
        val recipientId = if(activeAccount.domain != Contact.mainDomain)
            activeAccount.recipientId.plus("@${activeAccount.domain}")
        else
            activeAccount.recipientId
        val registrationBundles = keyGenerator.register(recipientId,
                activeAccount.deviceId)

        if(remainingKeys.size < SignalKeyGenerator.PRE_KEY_COUNT) {
            val response = Result.of {
                mailboxAPIClient.insertPreKeys(
                        preKeys = registrationBundles.uploadBundle.preKeys,
                        excludedKeys = remainingKeys)
            }
            if (response is Result.Success) {
                val preKeyList = registrationBundles.privateBundle.preKeys.entries.map { (key, value) ->
                    CRPreKey(id = 0, preKeyId = key, byteString = value, accountId = activeAccount.id)
                }.filter { it.preKeyId !in remainingKeys }
                db.insertPreKeys(preKeyList)

                if (acknoledgeEvents)
                    acknowledgeEventsIgnoringErrors(listOf(event.rowid))
            }
        } else {
            if (acknoledgeEvents)
                acknowledgeEventsIgnoringErrors(listOf(event.rowid))
        }
    }

    private fun processLinkRequestEvents(event: Event) {
        if (acknoledgeEvents)
            acknowledgeEventsIgnoringErrors(listOf(event.rowid))
        linkDevicesEvents.add(DeviceInfo.UntrustedDeviceInfo.fromJSON(event.params))
        shouldNotify = true
    }

    private fun processSyncRequestEvents(event: Event) {
        if (acknoledgeEvents)
            acknowledgeEventsIgnoringErrors(listOf(event.rowid))

        linkDevicesEvents.add(DeviceInfo.TrustedDeviceInfo.fromJSON(event.params, null))
        shouldNotify = true
    }

    private fun processUpdateBannerData(event: Event) {
        val bannerData = UpdateBannerEventData.fromJSON(event.params)
        val operation = getImageFromCdn(bannerData)
        when(operation){
            is Result.Success ->{
                shouldNotify = true
                if (acknoledgeEvents)
                    eventsToAcknowldege.add(event.rowid)
                updateBannerData = operation.value
            }
            is Result.Failure -> updateBannerData = null
        }
    }

    private fun processNewEmails(event: Event) {
        val metadata = EmailMetadata.fromJSON(event.params)
        val operation = Result.of {
            insertIncomingEmailTransaction(metadata)
        }

        when(operation){
            is Result.Success -> {
                val newPreview = db.getEmailPreviewByMetadataKey(metadata.metadataKey, label.text, activeAccount)
                if(newPreview != null)
                    newEmails.add(newPreview)
                shouldNotify = true
                if (acknoledgeEvents)
                    eventsToAcknowldege.add(event.rowid)
            }
            is Result.Failure -> {
                when(operation.error){
                    is DuplicateMessageException -> {
                        updateExistingEmailTransaction(metadata)
                        if (acknoledgeEvents)
                            eventsToAcknowldege.add(event.rowid)
                    }
                }
            }
        }
    }

    private fun processThreadReadStatusChanged(event: Event) {
        val metadata = PeerReadThreadStatusUpdate.fromJSON(event.params)
        val operation = Result.of {
            updateThreadReadStatus(metadata)
        }
        when(operation){
            is Result.Success -> {
                threadReads.add(Pair(metadata.threadIds, metadata.unread))
                if (acknoledgeEvents)
                    eventsToAcknowldege.add(event.rowid)
            }
        }
    }

    private fun processEmailReadStatusChanged(event: Event) {
        val metadata = PeerReadEmailStatusUpdate.fromJSON(event.params)
        val operation = Result.of {
            updateEmailReadStatus(metadata)
        }
        when(operation){
            is Result.Success -> {
                emailReads.add(Pair(metadata.metadataKeys, metadata.unread))
                if (acknoledgeEvents)
                    eventsToAcknowldege.add(event.rowid)
            }
        }
    }

    private fun processUnsendEmailStatusChanged(event: Event) {
        val metadata = PeerUnsendEmailStatusUpdate.fromJSON(event.params)
        val operation = Result.of {
            updateUnsendEmailStatus(metadata)
        }
        when(operation){
            is Result.Success -> {
                unsend = Pair(metadata.metadataKey, metadata.unsendDate)
                if (acknoledgeEvents)
                    eventsToAcknowldege.add(event.rowid)
            }
        }
    }

    private fun processPeerUsernameChanged(event: Event) {
        val metadata = PeerUsernameChangedStatusUpdate.fromJSON(event.params)
        val operation = Result.of {
            updateUsernameStatus(metadata)
        }
        when(operation){
            is Result.Success -> {
                nameChanged = metadata.name
                if (acknoledgeEvents)
                    eventsToAcknowldege.add(event.rowid)
            }
        }
    }

    private fun processEmailLabelChanged(event: Event) {
        val metadata = PeerEmailLabelsChangedStatusUpdate.fromJSON(event.params)

        val operation = Result.of {
            updateEmailLabelChangedStatus(metadata)
        }
        when(operation){
            is Result.Success -> {
                val added = db.getLabels(metadata.labelsAdded, activeAccount.id)
                val deleted = db.getLabels(metadata.labelsRemoved, activeAccount.id)
                movedEmail.add(Triple(metadata.metadataKeys, added, deleted))
                if (acknoledgeEvents)
                    eventsToAcknowldege.add(event.rowid)
            }
        }
    }

    private fun processThreadLabelChanged(event: Event) {
        val metadata = PeerThreadLabelsChangedStatusUpdate.fromJSON(event.params)
        val operation = Result.of {
            updateThreadLabelChangedStatus(metadata)
        }
        when(operation){
            is Result.Success -> {
                val added = db.getLabels(metadata.labelsAdded, activeAccount.id)
                val deleted = db.getLabels(metadata.labelsRemoved, activeAccount.id)
                movedThread.add(Triple(metadata.threadIds, added, deleted))
                if (acknoledgeEvents)
                    eventsToAcknowldege.add(event.rowid)
            }
        }
    }

    private fun processEmailDeletedPermanently(event: Event) {
        val metadata = PeerEmailDeletedStatusUpdate.fromJSON(event.params)
        val operation = Result.of {
            updateEmailDeletedPermanentlyStatus(metadata)
        }
        when(operation){
            is Result.Success -> {
                movedEmail.add(Triple(metadata.metadataKeys, null, null))
                if (acknoledgeEvents)
                    eventsToAcknowldege.add(event.rowid)
            }
        }
    }

    private fun processThreadDeletedPermanently(event: Event) {
        val metadata = PeerThreadDeletedStatusUpdate.fromJSON(event.params)
        val operation = Result.of {
            updateThreadDeletedPermanentlyStatus(metadata)
        }
        when(operation){
            is Result.Success -> {
                movedThread.add(Triple(metadata.threadIds, null, null))
                if (acknoledgeEvents)
                    eventsToAcknowldege.add(event.rowid)
            }
        }
    }

    private fun processLabelCreated(event: Event) {
        val metadata = PeerLabelCreatedStatusUpdate.fromJSON(event.params)
        val operation = Result.of {
            updateLabelCreatedStatus(metadata)
        }
        when(operation){
            is Result.Success -> {
                customLabels = db.getCustomLabels(activeAccount.id).toMutableList()
                if (acknoledgeEvents)
                    eventsToAcknowldege.add(event.rowid)
            }
        }
    }

    private fun processLabelEdited(event: Event) {
        val metadata = PeerLabelEditedStatusUpdate.fromJSON(event.params)
        val operation = Result.of {
            updateLabelEditedStatus(metadata)
        }
        when(operation){
            is Result.Success -> {
                customLabels = db.getCustomLabels(activeAccount.id).toMutableList()
                if (acknoledgeEvents)
                    eventsToAcknowldege.add(event.rowid)
            }
        }
    }

    private fun processLabelDeleted(event: Event) {
        val metadata = PeerLabelDeletedStatusUpdate.fromJSON(event.params)
        val operation = Result.of {
            updateLabelDeletedStatus(metadata)
        }
        when(operation){
            is Result.Success -> {
                customLabels = db.getCustomLabels(activeAccount.id).toMutableList()
                if (acknoledgeEvents)
                    eventsToAcknowldege.add(event.rowid)
            }
        }
    }

    private fun processTrackingUpdates(event: Event) {
        val operation = Result.of {
            val metadata = TrackingUpdate.fromJSON(event.params)

            createFeedItems(listOf(metadata))
            changeDeliveryTypes(listOf(metadata))

            if (metadata.type == DeliveryTypes.UNSEND) {
                updateUnsendEmailStatus(PeerUnsendEmailStatusUpdate(metadata.metadataKey, metadata.date))
            }
        }
        when(operation){
            is Result.Success -> {
                if(acknoledgeEvents)
                    eventsToAcknowldege.add(event.rowid)
            }
        }


    }

    private fun processOnError(event: Event) {
        eventsToAcknowldege.add(event.rowid)
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
        db.updateUserName(activeAccount.recipientId, activeAccount.domain, metadata.name, activeAccount.id)
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

    private fun updateLabelDeletedStatus(metadata: PeerLabelDeletedStatusUpdate) =
            db.updateDeleteLabel(metadata.uuid, activeAccount.id)

    private fun updateLabelEditedStatus(metadata: PeerLabelEditedStatusUpdate) =
            db.updateEditLabel(metadata.uuid, metadata.name, activeAccount.id)

    private fun updateExistingEmailTransaction(metadata: EmailMetadata) =
            db.updateExistingEmail(metadata, activeAccount)


    private fun changeDeliveryTypeByMetadataKey(metadataKeys: List<Long>, deliveryType: DeliveryTypes) =
            db.updateDeliveryTypeByMetadataKey(metadataKeys, deliveryType, activeAccount.id)

    private fun createFeedItems(trackingUpdates: List<TrackingUpdate>) =
            db.updateFeedItems(trackingUpdates, activeAccount.id)


    private fun acknowledgeEventsIgnoringErrors(eventIdsToAcknowledge: List<Long>): Boolean {
        try {
            if(eventIdsToAcknowledge.isNotEmpty() && acknoledgeEvents)
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
    class NoContentFoundException: Exception()
}