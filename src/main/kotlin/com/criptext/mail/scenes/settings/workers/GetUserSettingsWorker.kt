package com.criptext.mail.scenes.settings.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.data.SettingsAPIClient
import com.criptext.mail.scenes.settings.data.SettingsResult
import com.criptext.mail.scenes.settings.data.UserSettingsData
import com.criptext.mail.utils.ServerErrorCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError


class GetUserSettingsWorker(
        private val httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        private val accountDao: AccountDao,
        private val storage: KeyValueStorage,
        override val publishFn: (
                SettingsResult.GetUserSettings) -> Unit)
    : BackgroundWorker<SettingsResult.GetUserSettings> {

    override val canBeParallelized = true
    private val apiClient = SettingsAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): SettingsResult.GetUserSettings {
        return if(ex is ServerErrorException) {
            when {
                ex.errorCode == ServerErrorCodes.DeviceRemoved ->
                    SettingsResult.GetUserSettings.Unauthorized(UIMessage(R.string.device_removed_remotely_exception))
                ex.errorCode == ServerErrorCodes.Forbidden ->
                    SettingsResult.GetUserSettings.Forbidden()
                else -> SettingsResult.GetUserSettings.Failure(createErrorMessage(ex))
            }
        }
        else SettingsResult.GetUserSettings.Failure(createErrorMessage(ex))
    }

    override fun work(reporter: ProgressReporter<SettingsResult.GetUserSettings>): SettingsResult.GetUserSettings? {
        val getSettingsOperation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(getSettingsOperation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            getSettingsOperation

        return when (finalResult){
            is Result.Success -> {
                val settings = UserSettingsData.fromJSON(finalResult.value)
                val devices = settings.devices.map { if(it.id == activeAccount.deviceId) it.copy(
                        isCurrent = true
                ) else it }
                SettingsResult.GetUserSettings.Success(settings.copy(
                        devices = (devices.filter { it.isCurrent } + devices.filter { !it.isCurrent }).sorted()
                ))
            }
            is Result.Failure -> {
                catchException(finalResult.error)
            }
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private fun workOperation() : Result<String, Exception> = Result.of {apiClient.getSettings()}
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

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is ServerErrorException -> UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
            else -> UIMessage(resId = R.string.error_listing_devices)
        }
    }

}
