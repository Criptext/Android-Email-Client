package com.criptext.mail.utils.removedevice.data

import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.utils.removedevice.RemoveDeviceUtils
import com.github.kittinunf.result.Result

class DeviceRemovedWorker(private val db: AppDatabase,
                          private val storage: KeyValueStorage,
                          override val publishFn: (RemovedDeviceResult.DeviceRemoved) -> Unit
                          ) : BackgroundWorker<RemovedDeviceResult.DeviceRemoved> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): RemovedDeviceResult.DeviceRemoved {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(reporter: ProgressReporter<RemovedDeviceResult.DeviceRemoved>)
            : RemovedDeviceResult.DeviceRemoved? {

        val deleteOperation = Result.of {
            RemoveDeviceUtils.removeDevice(db, storage)
        }
        return when (deleteOperation){
            is Result.Success -> {
                RemovedDeviceResult.DeviceRemoved.Success()
            }
            is Result.Failure -> {
                RemovedDeviceResult.DeviceRemoved.Failure()
            }
        }
    }

    override fun cancel() {
        TODO("not implemented")
    }
}