package com.criptext.mail.websocket.data

import com.criptext.mail.api.EmailInsertionAPIClient
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.*
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.data.RemoveDeviceWorker
import com.criptext.mail.signal.SignalClient

/**
 * Created by gabriel on 5/1/18.
 */

class EventDataSource(override val runner: WorkRunner,
                      private val db : AppDatabase,
                      private val emailInsertionAPIClient: EmailInsertionAPIClient,
                      private val signalClient: SignalClient,
                      private val activeAccount: ActiveAccount,
                      private val storage: KeyValueStorage,
                      private val httpClient: HttpClient): BackgroundWorkManager<EventRequest, EventResult>() {

    override fun createWorkerFromParams(params: EventRequest, flushResults: (EventResult) -> Unit): BackgroundWorker<*> {
        return when (params) {
            is EventRequest.InsertNewEmail -> InsertNewEmailWorker(
                    emailInsertionDao = db.emailInsertionDao(), signalClient = signalClient,
                    emailInsertionApi = emailInsertionAPIClient, metadata = params.emailMetadata,
                    activeAccount = activeAccount,
                    publishFn = flushResults)
            is EventRequest.UpdateDeliveryStatus -> UpdateDeliveryStatusWorker(
                    dao = db.emailDao(), fileDao = db.fileDao(), trackingUpdate = params.trackingUpdate,
                    feedDao = db.feedDao(), contactDao = db.contactDao(), publishFn = flushResults)
            is EventRequest.UpdatePeerUnsendEmailStatus -> UpdatePeerUnsendEmailStatusWorker(
                    dao = db.emailDao(), peerUnsendEmailStatusUpdate = params.peerUnsendEmailStatusUpdate,
                    fileDao = db.fileDao(), publishFn = flushResults, activeAccount = activeAccount,
                    httpClient = httpClient, eventId = params.eventId)
            is EventRequest.UpdatePeerReadEmailStatus -> UpdatePeerReadStatusWorker(
                    dao = db.emailDao(), peerReadEmailStatusUpdate = params.peerReadEmailStatusUpdate,
                    publishFn = flushResults, eventId = params.eventId, httpClient = httpClient,
                    activeAccount = activeAccount)
            is EventRequest.UpdatePeerReadThreadStatus -> UpdatePeerReadThreadStatusWorker(
                    dao = db.emailDao(), peerReadThreadStatusUpdate = params.peerReadThreadStatusUpdate,
                    publishFn = flushResults, activeAccount = activeAccount, httpClient = httpClient,
                    eventId = params.eventId)
            is EventRequest.UpdatePeerEmailDeletedStatus -> UpdatePeerEmailDeletedStatusWorker(
                    dao = db.emailDao(), peerEmailDeletedStatusUpdate = params.peerEmailDeleted,
                    publishFn = flushResults, eventId = params.eventId, httpClient = httpClient,
                    activeAccount = activeAccount)
            is EventRequest.UpdatePeerThreadDeletedStatus -> UpdatePeerThreadDeletedStatusWorker(
                    dao = db.emailDao(), peerThreadDeletedStatusUpdate = params.peerThreadDeleted,
                    publishFn = flushResults, activeAccount = activeAccount, httpClient = httpClient,
                    eventId = params.eventId)
            is EventRequest.UpdatePeerEmailLabelsChangedStatus -> UpdatePeerEmailLabelsChangedStatusWorker(
                    dao = db.emailDao(), peerEmailLabelsChangedStatusUpdate = params.peerEmailLabelsChanged,
                    emailLabelDao = db.emailLabelDao(), labelDao = db.labelDao(),
                    publishFn = flushResults, eventId = params.eventId, httpClient = httpClient,
                    activeAccount = activeAccount)
            is EventRequest.UpdatePeerThreadLabelsChangedStatus -> UpdatePeerThreadLabelsChangedStatusWorker(
                    dao = db.emailDao(), peerThreadLabelsChangedStatusUpdate = params.peerThreadsLabelChanged,
                    publishFn = flushResults, labelDao = db.labelDao(), emailLabelDao = db.emailLabelDao(),
                    activeAccount = activeAccount, httpClient = httpClient, eventId = params.eventId)
            is EventRequest.UpdatePeerLabelCreatedStatus -> UpdatePeerLabelCreatedStatusWorker(
                    labelDao = db.labelDao(), peerLabelCreatedStatusUpdate = params.peerLabelCreated,
                    publishFn = flushResults, eventId = params.eventId, httpClient = httpClient,
                    activeAccount = activeAccount)
            is EventRequest.UpdatePeerUsernameChangedStatus -> UpdatePeerUsernameChangedStatusWorker(
                    contactDao = db.contactDao(), peerUsernameChangedStatusUpdate = params.peerUsernameChanged,
                    publishFn = flushResults, accountDao = db.accountDao(), activeAccount = activeAccount,
                    httpClient = httpClient, eventId = params.eventId)
            is EventRequest.DeviceRemoved -> DeviceRemovedWorker(
                    db = db, storage = storage, publishFn = flushResults
            )
        }
    }
}