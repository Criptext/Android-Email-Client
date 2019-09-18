package com.criptext.mail.scenes.settings.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.workers.*
import com.criptext.mail.utils.generaldatasource.workers.GetUserSettingsWorker

class SettingsDataSource(
        private val settingsLocalDB: SettingsLocalDB,
        private val storage: KeyValueStorage,
        var activeAccount: ActiveAccount,
        private val httpClient: HttpClient,
        override val runner: WorkRunner)
    : BackgroundWorkManager<SettingsRequest, SettingsResult>(){

    override fun createWorkerFromParams(params: SettingsRequest,
                                        flushResults: (SettingsResult) -> Unit): BackgroundWorker<*> {

        return when(params){
            is SettingsRequest.ResetPassword -> ForgotPasswordWorker(
                    storage = storage,
                    accountDao = settingsLocalDB.accountDao,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    publishFn = { res -> flushResults(res) }
            )
            is SettingsRequest.SyncBegin -> SyncBeginWorker(
                    httpClient = httpClient,
                    storage = storage,
                    accountDao = settingsLocalDB.accountDao,
                    activeAccount = activeAccount,
                    publishFn = { res -> flushResults(res) }
            )
        }
    }
}