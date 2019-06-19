package com.criptext.mail.scenes.settings.replyto.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.replyto.workers.ChangeReplyToEmailWorker

class ReplyToDataSource(
        private val settingsLocalDB: SettingsLocalDB,
        private val storage: KeyValueStorage,
        var activeAccount: ActiveAccount,
        private val httpClient: HttpClient,
        override val runner: WorkRunner)
    : BackgroundWorkManager<ReplyToRequest, ReplyToResult>(){

    override fun createWorkerFromParams(params: ReplyToRequest,
                                        flushResults: (ReplyToResult) -> Unit): BackgroundWorker<*> {

        return when(params){
            is ReplyToRequest.SetReplyToEmail -> ChangeReplyToEmailWorker(
                    httpClient = httpClient,
                    storage = storage,
                    accountDao = settingsLocalDB.accountDao,
                    activeAccount = activeAccount,
                    newEmail = params.newEmail,
                    enabled = params.enabled,
                    publishFn = { res -> flushResults(res) }
            )
        }
    }
}