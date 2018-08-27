package com.criptext.mail.scenes.settings.change_email.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount

class ChangeEmailDataSource(
        private val storage: KeyValueStorage,
        private val activeAccount: ActiveAccount,
        private val httpClient: HttpClient,
        override val runner: WorkRunner)
    : BackgroundWorkManager<ChangeEmailRequest, ChangeEmailResult>(){

    override fun createWorkerFromParams(params: ChangeEmailRequest,
                                        flushResults: (ChangeEmailResult) -> Unit): BackgroundWorker<*> {

        return when(params){
            is ChangeEmailRequest.ChangeRecoveryEmail -> ChangeRecoveryEmailWorker(
                    newEmail = params.newRecoveryEmail,
                    password = params.password,
                    publishFn = flushResults,
                    httpClient = httpClient,
                    activeAccount = activeAccount
            )
        }
    }
}