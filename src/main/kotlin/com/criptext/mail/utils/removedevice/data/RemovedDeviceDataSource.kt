package com.criptext.mail.utils.removedevice.data

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

class RemovedDeviceDataSource(override val runner: WorkRunner,
                              private val db : AppDatabase,
                              private val storage: KeyValueStorage): BackgroundWorkManager<RemovedDeviceRequest, RemovedDeviceResult>() {

    override fun createWorkerFromParams(params: RemovedDeviceRequest, flushResults: (RemovedDeviceResult) -> Unit): BackgroundWorker<*> {
        return when (params) {
            is RemovedDeviceRequest.DeviceRemoved -> DeviceRemovedWorker(
                    db = db, storage = storage, publishFn = flushResults
            )
        }
    }
}