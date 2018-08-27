package com.criptext.mail.utils.remotechange.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount

class RemoteChangeDataSource(override val runner: WorkRunner,
                             private val db : AppDatabase,
                             private val storage: KeyValueStorage,
                             private val activeAccount: ActiveAccount,
                             private val httpClient: HttpClient
): BackgroundWorkManager<RemoteChangeRequest, RemoteChangeResult>() {

    override fun createWorkerFromParams(params: RemoteChangeRequest, flushResults: (RemoteChangeResult) -> Unit): BackgroundWorker<*> {
        return when (params) {
            is RemoteChangeRequest.DeviceRemoved -> DeviceRemovedWorker(
                    activeAccount = activeAccount, httpClient = httpClient,
                    db = db, storage = storage, publishFn = flushResults
            )
            is RemoteChangeRequest.ConfirmPassword -> ConfirmPasswordWorker(
                    activeAccount = activeAccount, httpClient = httpClient,
                    password = params.password, publishFn = flushResults
            )
        }
    }
}