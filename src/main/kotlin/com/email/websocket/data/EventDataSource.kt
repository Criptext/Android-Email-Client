package com.email.websocket.data

import com.email.api.EmailInsertionAPIClient
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.BackgroundWorkManager
import com.email.bgworker.WorkRunner
import com.email.db.dao.ContactDao
import com.email.db.dao.EmailDao
import com.email.db.dao.EmailInsertionDao
import com.email.db.models.ActiveAccount
import com.email.db.dao.FeedItemDao
import com.email.signal.SignalClient

/**
 * Created by gabriel on 5/1/18.
 */

class EventDataSource(override val runner: WorkRunner,
                      private val feedItemDao: FeedItemDao,
                      private val emailDao: EmailDao,
                      private val contactDao: ContactDao,
                      private val emailInsertionDao: EmailInsertionDao,
                      private val emailInsertionAPIClient: EmailInsertionAPIClient,
                      private val signalClient: SignalClient,
                      private val activeAccount: ActiveAccount): BackgroundWorkManager<EventRequest, EventResult>() {

    override fun createWorkerFromParams(params: EventRequest, flushResults: (EventResult) -> Unit): BackgroundWorker<*> {
        return when (params) {
            is EventRequest.InsertNewEmail -> InsertNewEmailWorker(
                    emailInsertionDao = emailInsertionDao, signalClient = signalClient,
                    emailInsertionApi = emailInsertionAPIClient, metadata = params.emailMetadata,
                    activeAccount = activeAccount,
                    publishFn = flushResults)
            is EventRequest.UpdateDeliveryStatus -> UpdateDeliveryStatusWorker(
                    dao = emailDao, trackingUpdate = params.trackingUpdate,
                    feedDao = feedItemDao, contactDao = contactDao, publishFn = flushResults)
        }
    }
}