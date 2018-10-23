package com.criptext.mail.push.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalStoreCriptext

class PushDataSource(
        private val httpClient: HttpClient,
        override val runner: WorkRunner,
        private val db: AppDatabase)
    : BackgroundWorkManager<PushRequest, PushResult>() {
    override fun createWorkerFromParams(
            params: PushRequest,
            flushResults: (PushResult) -> Unit)
            : BackgroundWorker<*> {
        val activeAccount = ActiveAccount.loadFromDB(db.accountDao().getLoggedInAccount()!!)!!
        return when (params) {
            is PushRequest.UpdateMailbox -> UpdateMailboxWorker(
                    signalClient = SignalClient.Default(SignalStoreCriptext(db)),
                    dbEvents = EventLocalDB(db),
                    httpClient = httpClient,
                    activeAccount = activeAccount,
                    label = params.label,
                    loadedThreadsCount = params.loadedThreadsCount,
                    pushData = params.pushData,
                    shouldPostNotification = params.shouldPostNotification,
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
        }
    }

}
