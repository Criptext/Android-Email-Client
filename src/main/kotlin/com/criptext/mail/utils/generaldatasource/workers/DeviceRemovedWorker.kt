package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.utils.generaldatasource.RemoveDeviceUtils
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap

class DeviceRemovedWorker(private val letAPIKnow: Boolean,
                          private val activeAccount: ActiveAccount,
                          httpClient: HttpClient,
                          private val db: AppDatabase,
                          private val storage: KeyValueStorage,
                          override val publishFn: (GeneralResult.DeviceRemoved) -> Unit
                          ) : BackgroundWorker<GeneralResult.DeviceRemoved> {

    override val canBeParallelized = false
    private val apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): GeneralResult.DeviceRemoved {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(reporter: ProgressReporter<GeneralResult.DeviceRemoved>)
            : GeneralResult.DeviceRemoved? {

        val deleteOperation = if(letAPIKnow) { Result.of { apiClient.postLogout() }
                .flatMap { Result.of { RemoveDeviceUtils.clearAllData(db, storage) }}}
        else
            Result.of { RemoveDeviceUtils.clearAllData(db, storage) }
        return when (deleteOperation){
            is Result.Success -> {
                GeneralResult.DeviceRemoved.Success()
            }
            is Result.Failure -> {
                GeneralResult.DeviceRemoved.Failure()
            }
        }
    }

    override fun cancel() {
        TODO("not implemented")
    }
}