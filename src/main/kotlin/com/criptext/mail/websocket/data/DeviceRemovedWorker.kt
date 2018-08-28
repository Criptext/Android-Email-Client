package com.criptext.mail.websocket.data

import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.utils.remotechange.RemoveDeviceUtils
import com.github.kittinunf.result.Result

class DeviceRemovedWorker(private val db: AppDatabase,
                          private val storage: KeyValueStorage,
                          override val publishFn: (EventResult.DeviceRemoved) -> Unit
                          ) : BackgroundWorker<EventResult.DeviceRemoved> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): EventResult.DeviceRemoved {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(reporter: ProgressReporter<EventResult.DeviceRemoved>)
            : EventResult.DeviceRemoved? {

        val deleteOperation = Result.of {
            RemoveDeviceUtils.removeDevice(db, storage)
        }
        return when (deleteOperation){
            is Result.Success -> {
                EventResult.DeviceRemoved.Success()
            }
            is Result.Failure -> {
                EventResult.DeviceRemoved.Failure()
            }
        }
    }

    override fun cancel() {
        TODO("not implemented")
    }
}