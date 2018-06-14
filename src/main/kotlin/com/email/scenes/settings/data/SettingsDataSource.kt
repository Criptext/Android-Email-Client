package com.email.scenes.settings.data

import com.email.bgworker.BackgroundWorkManager
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.WorkRunner
import com.email.db.SettingsLocalDB

class SettingsDataSource(
        private val settingsLocalDB: SettingsLocalDB,
        override val runner: WorkRunner)
    : BackgroundWorkManager<SettingsRequest, SettingsResult>(){

    override fun createWorkerFromParams(params: SettingsRequest,
                                        flushResults: (SettingsResult) -> Unit): BackgroundWorker<*> {

        return when(params){
            is SettingsRequest.ChangeContactName -> ChangeContactNameWorker(
                    fullName = params.fullName,
                    recipientId = params.recipientId,
                    settingsLocalDB = settingsLocalDB,
                    publishFn = { res -> flushResults(res) }
            )
            is SettingsRequest.GetCustomLabels -> GetCustomLabelsWorker(
                    db = settingsLocalDB,
                    publishFn = { res -> flushResults(res) }
            )
        }
    }
}