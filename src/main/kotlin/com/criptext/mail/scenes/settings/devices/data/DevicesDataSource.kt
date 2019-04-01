package com.criptext.mail.scenes.settings.devices.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.devices.workers.RemoveDeviceWorker

class DevicesDataSource(
        private val accountDao: AccountDao,
        private val storage: KeyValueStorage,
        private val activeAccount: ActiveAccount,
        private val httpClient: HttpClient,
        override val runner: WorkRunner)
    : BackgroundWorkManager<DevicesRequest, DevicesResult>(){

    override fun createWorkerFromParams(params: DevicesRequest,
                                        flushResults: (DevicesResult) -> Unit): BackgroundWorker<*> {

        return when(params){
            is DevicesRequest.RemoveDevice -> RemoveDeviceWorker(
                    storage = storage,
                    accountDao = accountDao,
                    password = params.password,
                    deviceId = params.deviceId,
                    position = params.position,
                    httpClient = httpClient,
                    activeAccount = activeAccount,
                    publishFn = { res -> flushResults(res) }
            )
        }
    }
}