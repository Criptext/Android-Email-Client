package com.criptext.mail.push.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.push.workers.GetPushEmailWorker
import com.criptext.mail.push.workers.RemoveNotificationWorker
import com.criptext.mail.push.workers.UpdateMailboxWorker
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalStoreCriptext
import java.io.File

class PushDataSource(
        private val filesDir: File,
        private val cacheDir: File,
        private val httpClient: HttpClient,
        override val runner: WorkRunner,
        private val db: AppDatabase,
        private val activeAccount: ActiveAccount,
        private val storage: KeyValueStorage)
    : BackgroundWorkManager<PushRequest, PushResult>() {
    override fun createWorkerFromParams(
            params: PushRequest,
            flushResults: (PushResult) -> Unit)
            : BackgroundWorker<*> {
        return when (params) {
            is PushRequest.NewEmail -> GetPushEmailWorker(
                    db = db,
                    dbEvents = EventLocalDB(db, filesDir, cacheDir),
                    httpClient = httpClient,
                    label = params.label,
                    pushData = params.pushData,
                    shouldPostNotification = params.shouldPostNotification,
                    publishFn = { result ->
                        flushResults(result)
                    })
            is PushRequest.UpdateMailbox -> UpdateMailboxWorker(
                    signalClient = SignalClient.Default(SignalStoreCriptext(db, activeAccount)),
                    dbEvents = EventLocalDB(db, filesDir, cacheDir),
                    httpClient = httpClient,
                    storage = storage,
                    activeAccount = activeAccount,
                    label = params.label,
                    unableToDecryptLocalized = params.unableToDecryptLocalized,
                    publishFn = { result ->
                        flushResults(result)
                    })
            is PushRequest.LinkAccept -> LinkAuthAcceptWorker(
                    activeAccount = activeAccount, httpClient = httpClient,
                    deviceId = params.randomId,
                    notificationId = params.notificationId,
                    publishFn = { res -> flushResults(res)}
            )
            is PushRequest.LinkDenied -> LinkAuthDenyWorker(
                    activeAccount = activeAccount, httpClient = httpClient,
                    deviceId = params.randomId,
                    notificationId = params.notificationId,
                    publishFn = { res -> flushResults(res)}
            )
            is PushRequest.RemoveNotification -> RemoveNotificationWorker(
                    db = db,
                    notificationValue = params.value,
                    pushData = params.pushData,
                    publishFn = { res -> flushResults(res)}
            )
        }
    }

}
