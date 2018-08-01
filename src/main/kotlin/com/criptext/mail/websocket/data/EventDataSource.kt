package com.criptext.mail.websocket.data

import com.criptext.mail.api.EmailInsertionAPIClient
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.dao.*
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.signal.SignalClient

/**
 * Created by gabriel on 5/1/18.
 */

class EventDataSource(override val runner: WorkRunner,
                      private val feedItemDao: FeedItemDao,
                      private val fileDao: FileDao,
                      private val emailDao: EmailDao,
                      private val emailLabelDao: EmailLabelDao,
                      private val labelDao: LabelDao,
                      private val contactDao: ContactDao,
                      private val accountDao: AccountDao,
                      private val emailInsertionDao: EmailInsertionDao,
                      private val emailInsertionAPIClient: EmailInsertionAPIClient,
                      private val signalClient: SignalClient,
                      private val activeAccount: ActiveAccount,
                      private val httpClient: HttpClient): BackgroundWorkManager<EventRequest, EventResult>() {

    override fun createWorkerFromParams(params: EventRequest, flushResults: (EventResult) -> Unit): BackgroundWorker<*> {
        return when (params) {
            is EventRequest.InsertNewEmail -> InsertNewEmailWorker(
                    emailInsertionDao = emailInsertionDao, signalClient = signalClient,
                    emailInsertionApi = emailInsertionAPIClient, metadata = params.emailMetadata,
                    activeAccount = activeAccount,
                    publishFn = flushResults)
            is EventRequest.UpdateDeliveryStatus -> UpdateDeliveryStatusWorker(
                    dao = emailDao, fileDao = fileDao, trackingUpdate = params.trackingUpdate,
                    feedDao = feedItemDao, contactDao = contactDao, publishFn = flushResults)
            is EventRequest.UpdatePeerUnsendEmailStatus -> UpdatePeerUnsendEmailStatusWorker(
                    dao = emailDao, peerUnsendEmailStatusUpdate = params.peerUnsendEmailStatusUpdate,
                    fileDao = fileDao, publishFn = flushResults, activeAccount = activeAccount,
                    httpClient = httpClient, eventId = params.eventId)
            is EventRequest.UpdatePeerReadEmailStatus -> UpdatePeerReadStatusWorker(
                    dao = emailDao, peerReadEmailStatusUpdate = params.peerReadEmailStatusUpdate,
                    publishFn = flushResults, eventId = params.eventId, httpClient = httpClient,
                    activeAccount = activeAccount)
            is EventRequest.UpdatePeerReadThreadStatus -> UpdatePeerReadThreadStatusWorker(
                    dao = emailDao, peerReadThreadStatusUpdate = params.peerReadThreadStatusUpdate,
                    publishFn = flushResults, activeAccount = activeAccount, httpClient = httpClient,
                    eventId = params.eventId)
            is EventRequest.UpdatePeerEmailDeletedStatus -> UpdatePeerEmailDeletedStatusWorker(
                    dao = emailDao, peerEmailDeletedStatusUpdate = params.peerEmailDeleted,
                    publishFn = flushResults, eventId = params.eventId, httpClient = httpClient,
                    activeAccount = activeAccount)
            is EventRequest.UpdatePeerThreadDeletedStatus -> UpdatePeerThreadDeletedStatusWorker(
                    dao = emailDao, peerThreadDeletedStatusUpdate = params.peerThreadDeleted,
                    publishFn = flushResults, activeAccount = activeAccount, httpClient = httpClient,
                    eventId = params.eventId)
            is EventRequest.UpdatePeerEmailLabelsChangedStatus -> UpdatePeerEmailLabelsChangedStatusWorker(
                    dao = emailDao, peerEmailLabelsChangedStatusUpdate = params.peerEmailLabelsChanged,
                    emailLabelDao = emailLabelDao, labelDao = labelDao,
                    publishFn = flushResults, eventId = params.eventId, httpClient = httpClient,
                    activeAccount = activeAccount)
            is EventRequest.UpdatePeerThreadLabelsChangedStatus -> UpdatePeerThreadLabelsChangedStatusWorker(
                    dao = emailDao, peerThreadLabelsChangedStatusUpdate = params.peerThreadsLabelChanged,
                    publishFn = flushResults, labelDao = labelDao, emailLabelDao = emailLabelDao,
                    activeAccount = activeAccount, httpClient = httpClient, eventId = params.eventId)
            is EventRequest.UpdatePeerLabelCreatedStatus -> UpdatePeerLabelCreatedStatusWorker(
                    labelDao = labelDao, peerLabelCreatedStatusUpdate = params.peerLabelCreated,
                    publishFn = flushResults, eventId = params.eventId, httpClient = httpClient,
                    activeAccount = activeAccount)
            is EventRequest.UpdatePeerUsernameChangedStatus -> UpdatePeerUsernameChangedStatusWorker(
                    contactDao = contactDao, peerUsernameChangedStatusUpdate = params.peerUsernameChanged,
                    publishFn = flushResults, accountDao = accountDao, activeAccount = activeAccount,
                    httpClient = httpClient, eventId = params.eventId)
        }
    }
}