package com.criptext.mail.scenes.settings.workers

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.data.SettingsAPIClient
import com.criptext.mail.scenes.settings.data.SettingsResult
import com.criptext.mail.utils.sha256
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError


class RemoveDeviceWorker(
        private val password: String,
        private val deviceId: Int,
        private val position: Int,
        private val httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        private val accountDao: AccountDao,
        private val storage: KeyValueStorage,
        override val publishFn: (
                SettingsResult.RemoveDevice) -> Unit)
    : BackgroundWorker<SettingsResult.RemoveDevice> {

    override val canBeParallelized = true
    private val apiClient = SettingsAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): SettingsResult.RemoveDevice {
        return SettingsResult.RemoveDevice.Failure()
    }

    override fun work(reporter: ProgressReporter<SettingsResult.RemoveDevice>): SettingsResult.RemoveDevice? {
        val deleteOperation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(deleteOperation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            deleteOperation

        return when (finalResult){
            is Result.Success -> {
                SettingsResult.RemoveDevice.Success(deviceId, position)
            }
            is Result.Failure -> {
                SettingsResult.RemoveDevice.Failure()
            }
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private fun workOperation() : Result<String, Exception> = Result.of {
        apiClient.deleteDevice(deviceId, password.sha256()).body
    }
    .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)

    private fun newRetryWithNewSessionOperation()
            : Result<String, Exception> {
        val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient,
                activeAccount, storage, accountDao)
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when(refreshOperation){
            is Result.Success -> {
                val account = ActiveAccount.loadFromStorage(storage)!!
                apiClient.token = account.jwt
                workOperation()
            }
            is Result.Failure -> {
                Result.of { throw refreshOperation.error }
            }
        }
    }

}
