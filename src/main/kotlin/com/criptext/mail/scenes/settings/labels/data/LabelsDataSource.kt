package com.criptext.mail.scenes.settings.labels.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.labels.workers.ChangeVisibilityLabelWorker
import com.criptext.mail.scenes.settings.labels.workers.CreateCustomLabelWorker
import com.criptext.mail.scenes.settings.labels.workers.GetCustomLabelsWorker

class LabelsDataSource(
        private val settingsLocalDB: SettingsLocalDB,
        private val storage: KeyValueStorage,
        private val activeAccount: ActiveAccount,
        private val httpClient: HttpClient,
        override val runner: WorkRunner)
    : BackgroundWorkManager<LabelsRequest, LabelsResult>(){

    override fun createWorkerFromParams(params: LabelsRequest,
                                        flushResults: (LabelsResult) -> Unit): BackgroundWorker<*> {

        return when(params){
            is LabelsRequest.GetCustomLabels -> GetCustomLabelsWorker(
                    db = settingsLocalDB,
                    publishFn = { res -> flushResults(res) }
            )
            is LabelsRequest.CreateCustomLabel -> CreateCustomLabelWorker(
                    labelName = params.labelName,
                    settingsLocalDB = settingsLocalDB,
                    httpClient = httpClient,
                    activeAccount = activeAccount,
                    storage = storage,
                    publishFn = { res -> flushResults(res) }
            )
            is LabelsRequest.ChangeVisibilityLabel -> ChangeVisibilityLabelWorker(
                    db = settingsLocalDB,
                    isVisible = params.isVisible,
                    labelId = params.labelId,
                    publishFn = { res -> flushResults(res) }
            )
        }
    }
}