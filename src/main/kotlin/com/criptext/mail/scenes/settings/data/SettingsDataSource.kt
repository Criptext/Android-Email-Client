package com.criptext.mail.scenes.settings.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.replyto.workers.ChangeReplyToEmailWorker
import com.criptext.mail.scenes.settings.workers.*

class SettingsDataSource(
        private val settingsLocalDB: SettingsLocalDB,
        private val storage: KeyValueStorage,
        private val activeAccount: ActiveAccount,
        private val httpClient: HttpClient,
        override val runner: WorkRunner)
    : BackgroundWorkManager<SettingsRequest, SettingsResult>(){

    override fun createWorkerFromParams(params: SettingsRequest,
                                        flushResults: (SettingsResult) -> Unit): BackgroundWorker<*> {

        return when(params){
            is SettingsRequest.ChangeContactName -> ChangeContactNameWorker(
                    fullName = params.fullName,
                    recipientId = params.recipientId,
                    settingsLocalDB = settingsLocalDB,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    storage = storage,
                    publishFn = { res -> flushResults(res) }
            )
            is SettingsRequest.GetCustomLabels -> GetCustomLabelsWorker(
                    db = settingsLocalDB,
                    publishFn = { res -> flushResults(res) }
            )
            is SettingsRequest.CreateCustomLabel -> CreateCustomLabelWorker(
                    labelName = params.labelName,
                    settingsLocalDB = settingsLocalDB,
                    httpClient = httpClient,
                    activeAccount = activeAccount,
                    storage = storage,
                    publishFn = { res -> flushResults(res) }
            )
            is SettingsRequest.ChangeVisibilityLabel -> ChangeVisibilityLabelWorker(
                    db = settingsLocalDB,
                    isVisible = params.isVisible,
                    labelId = params.labelId,
                    publishFn = { res -> flushResults(res) }
            )
            is SettingsRequest.Logout -> LogoutWorker(
                    storage = storage,
                    db = settingsLocalDB,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    publishFn = { res -> flushResults(res) }
            )
            is SettingsRequest.GetUserSettings -> GetUserSettingsWorker(
                    storage = storage,
                    accountDao = settingsLocalDB.accountDao,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    publishFn = { res -> flushResults(res) }
            )
            is SettingsRequest.RemoveDevice -> RemoveDeviceWorker(
                    storage = storage,
                    accountDao = settingsLocalDB.accountDao,
                    password = params.password,
                    deviceId = params.deviceId,
                    position = params.position,
                    httpClient = httpClient,
                    activeAccount = activeAccount,
                    publishFn = { res -> flushResults(res) }
            )
            is SettingsRequest.ResetPassword -> ForgotPasswordWorker(
                    storage = storage,
                    accountDao = settingsLocalDB.accountDao,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    publishFn = { res -> flushResults(res) }
            )
            is SettingsRequest.Set2FA -> TwoFAWorker(
                    storage = storage,
                    accountDao = settingsLocalDB.accountDao,
                    activeAccount = activeAccount,
                    twoFA = params.twoFA,
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