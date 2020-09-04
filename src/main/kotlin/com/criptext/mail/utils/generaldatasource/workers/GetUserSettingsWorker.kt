package com.criptext.mail.utils.generaldatasource.workers

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
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError


class GetUserSettingsWorker(
        private val httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        private val accountDao: AccountDao,
        private val storage: KeyValueStorage,
        override val publishFn: (
                GeneralResult.GetUserSettings) -> Unit)
    : BackgroundWorker<GeneralResult.GetUserSettings> {

    override val canBeParallelized = false
    private val apiClient = SettingsAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): GeneralResult.GetUserSettings {
        return if(ex is ServerErrorException) {
            when {
                ex.errorCode == ServerCodes.Unauthorized ->
                    GeneralResult.GetUserSettings.Unauthorized(UIMessage(R.string.device_removed_remotely_exception))
                ex.errorCode == ServerCodes.SessionExpired ->
                    GeneralResult.GetUserSettings.SessionExpired()
                ex.errorCode == ServerCodes.Forbidden ->
                    GeneralResult.GetUserSettings.Forbidden()
                ex.errorCode == ServerCodes.EnterpriseAccountSuspended ->
                    GeneralResult.GetUserSettings.EnterpriseSuspended()
                else -> GeneralResult.GetUserSettings.Failure(createErrorMessage(ex))
            }
        }
        else GeneralResult.GetUserSettings.Failure(createErrorMessage(ex))
    }

    override fun work(reporter: ProgressReporter<GeneralResult.GetUserSettings>): GeneralResult.GetUserSettings? {
        val getSettingsOperation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(getSettingsOperation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            getSettingsOperation

        return when (finalResult){
            is Result.Success -> {

                val devices = finalResult.value.devices.map { if(it.id == activeAccount.deviceId) it.copy(
                        isCurrent = true
                ) else it }
                GeneralResult.GetUserSettings.Success(finalResult.value.copy(
                        devices = (devices.filter { it.isCurrent }
                                + devices.filter { !it.isCurrent }
                                .filter { it.lastActivity != null }).sorted()
                                + devices.filter { it.lastActivity == null }
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

    private fun workOperation() : Result<UserSettingsData, Exception> = Result.of {apiClient.getSettings().body}
            .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
            .flatMap { Result.of {
                val settings = UserSettingsData.fromJSON(it, activeAccount.id)
                if(settings.customerType != activeAccount.type) {
                    accountDao.updateAccountType(settings.customerType, activeAccount.recipientId, activeAccount.domain)
                    activeAccount.updateAccountType(storage, settings.customerType)
                }
                val defaultAddress = settings.aliases.findLast { it.isDefault }
                if(defaultAddress != null && defaultAddress.rowId != activeAccount.defaultAddress){
                    accountDao.updateDefaultAddress(activeAccount.recipientId, activeAccount.domain,
                            defaultAddress.rowId)
                    activeAccount.updateAccountDefaultAddress(storage, defaultAddress.rowId)
                }
                settings
            } }

    private fun newRetryWithNewSessionOperation()
            : Result<UserSettingsData, Exception> {
        val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient,
                activeAccount, storage, accountDao)
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when(refreshOperation){
            is Result.Success -> {
                apiClient.token = refreshOperation.value
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
