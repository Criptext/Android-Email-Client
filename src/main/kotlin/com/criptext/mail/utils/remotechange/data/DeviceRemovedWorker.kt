package com.criptext.mail.utils.remotechange.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.utils.remotechange.RemoveDeviceUtils
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap

class DeviceRemovedWorker(private val activeAccount: ActiveAccount,
                          httpClient: HttpClient,
                          private val db: AppDatabase,
                          private val storage: KeyValueStorage,
                          override val publishFn: (RemoteChangeResult.DeviceRemoved) -> Unit
                          ) : BackgroundWorker<RemoteChangeResult.DeviceRemoved> {

    override val canBeParallelized = false
    private val apiClient = RemoteChangeAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): RemoteChangeResult.DeviceRemoved {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(reporter: ProgressReporter<RemoteChangeResult.DeviceRemoved>)
            : RemoteChangeResult.DeviceRemoved? {

        val deleteOperation = Result.of { apiClient.deleteDevice(activeAccount.deviceId) }
                .flatMap { Result.of { RemoveDeviceUtils.removeDevice(db, storage) }}
        return when (deleteOperation){
            is Result.Success -> {
                RemoteChangeResult.DeviceRemoved.Success()
            }
            is Result.Failure -> {
                RemoteChangeResult.DeviceRemoved.Failure()
            }
        }
    }

    override fun cancel() {
        TODO("not implemented")
    }
}