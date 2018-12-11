package com.criptext.mail.scenes.settings.recovery_email.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.recovery_email.workers.ChangeRecoveryEmailWorker
import com.criptext.mail.scenes.settings.recovery_email.workers.CheckPasswordWorker
import com.criptext.mail.scenes.settings.recovery_email.workers.ResendLinkWorker

class RecoveryEmailDataSource(
        private val accountDao: AccountDao,
        private val storage: KeyValueStorage,
        private val activeAccount: ActiveAccount,
        private val httpClient: HttpClient,
        override val runner: WorkRunner)
    : BackgroundWorkManager<RecoveryEmailRequest, RecoveryEmailResult>(){

    override fun createWorkerFromParams(params: RecoveryEmailRequest,
                                        flushResults: (RecoveryEmailResult) -> Unit): BackgroundWorker<*> {

        return when(params){
            is RecoveryEmailRequest.ResendConfirmationLink -> ResendLinkWorker(
                    accountDao = accountDao,
                    storage = storage,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    publishFn = flushResults
            )
            is RecoveryEmailRequest.CheckPassword -> CheckPasswordWorker(
                    storage = storage,
                    accountDao = accountDao,
                    password = params.password,
                    publishFn = flushResults,
                    httpClient = httpClient,
                    activeAccount = activeAccount
            )
            is RecoveryEmailRequest.ChangeRecoveryEmail -> ChangeRecoveryEmailWorker(
                    storage = storage,
                    accountDao = accountDao,
                    newEmail = params.newRecoveryEmail,
                    password = params.password,
                    publishFn = flushResults,
                    httpClient = httpClient,
                    activeAccount = activeAccount
            )
        }
    }
}