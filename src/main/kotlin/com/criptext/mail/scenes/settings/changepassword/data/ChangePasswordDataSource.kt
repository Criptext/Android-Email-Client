package com.criptext.mail.scenes.settings.changepassword.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount

class ChangePasswordDataSource(
        private val accountDao: AccountDao,
        private val storage: KeyValueStorage,
        private val activeAccount: ActiveAccount,
        private val httpClient: HttpClient,
        override val runner: WorkRunner)
    : BackgroundWorkManager<ChangePasswordRequest, ChangePasswordResult>(){

    override fun createWorkerFromParams(params: ChangePasswordRequest,
                                        flushResults: (ChangePasswordResult) -> Unit): BackgroundWorker<*> {

        return when(params){
            is ChangePasswordRequest.ChangePassword -> ChangePasswordWorker(
                    storage = storage,
                    accountDao = accountDao,
                    oldPassword = params.oldPassword,
                    password = params.newPassword,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    publishFn = { res -> flushResults(res) }
            )
        }
    }
}