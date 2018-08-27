package com.criptext.mail.scenes.settings.recovery_email.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount

class RecoveryEmailDataSource(
        private val storage: KeyValueStorage,
        private val activeAccount: ActiveAccount,
        private val httpClient: HttpClient,
        override val runner: WorkRunner)
    : BackgroundWorkManager<RecoveryEmailRequest, RecoveryEmailResult>(){

    override fun createWorkerFromParams(params: RecoveryEmailRequest,
                                        flushResults: (RecoveryEmailResult) -> Unit): BackgroundWorker<*> {

        return when(params){
            is RecoveryEmailRequest.ResendConfirmationLink -> ResendLinkWorker(
                    storage = storage,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    publishFn = flushResults
            )
            is RecoveryEmailRequest.CheckPassword -> CheckPasswordWorker(
                    password = params.password,
                    publishFn = flushResults,
                    httpClient = httpClient,
                    activeAccount = activeAccount
            )
        }
    }
}