package com.email.scenes.settings.data

import com.email.api.HttpClient
import com.email.bgworker.BackgroundWorkManager
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.WorkRunner
import com.email.db.SettingsLocalDB
import com.email.db.models.ActiveAccount

class SettingsDataSource(
        private val settingsLocalDB: SettingsLocalDB,
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
                    publishFn = { res -> flushResults(res) }
            )
            is SettingsRequest.ChangeVisibilityLabel -> ChangeVisibilityLabelWorker(
                    db = settingsLocalDB,
                    isVisible = params.isVisible,
                    labelId = params.labelId,
                    publishFn = { res -> flushResults(res) }
            )
        }
    }
}