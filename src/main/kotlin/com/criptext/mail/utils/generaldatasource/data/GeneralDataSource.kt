package com.criptext.mail.utils.generaldatasource.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.utils.generaldatasource.workers.ConfirmPasswordWorker
import com.criptext.mail.utils.generaldatasource.workers.DeviceRemovedWorker
import com.criptext.mail.utils.generaldatasource.workers.ForgotPasswordWorker

class GeneralDataSource(override val runner: WorkRunner,
                        private val db : AppDatabase,
                        private val storage: KeyValueStorage,
                        private val activeAccount: ActiveAccount,
                        private val httpClient: HttpClient
): BackgroundWorkManager<GeneralRequest, GeneralResult>() {

    override fun createWorkerFromParams(params: GeneralRequest, flushResults: (GeneralResult) -> Unit): BackgroundWorker<*> {
        return when (params) {
            is GeneralRequest.DeviceRemoved -> DeviceRemovedWorker(
                    letAPIKnow = params.letAPIKnow,
                    activeAccount = activeAccount, httpClient = httpClient,
                    db = db, storage = storage, publishFn = flushResults
            )
            is GeneralRequest.ConfirmPassword -> ConfirmPasswordWorker(
                    activeAccount = activeAccount, httpClient = httpClient,
                    password = params.password, publishFn = flushResults
            )
            is GeneralRequest.ResetPassword -> ForgotPasswordWorker(
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    publishFn = { res -> flushResults(res) }
            )
        }
    }
}