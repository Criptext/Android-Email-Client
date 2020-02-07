package com.criptext.mail.scenes.settings.devices.workers

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
import com.criptext.mail.scenes.settings.devices.data.DevicesResult
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.sha256
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import kotlinx.android.synthetic.main.restore_backup_dialog.view.*


class RemoveDeviceWorker(
        private val password: String,
        private val deviceId: Int,
        private val position: Int,
        private val httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        private val accountDao: AccountDao,
        private val storage: KeyValueStorage,
        override val publishFn: (
                DevicesResult.RemoveDevice) -> Unit)
    : BackgroundWorker<DevicesResult.RemoveDevice> {

    override val canBeParallelized = true
    private val apiClient = SettingsAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): DevicesResult.RemoveDevice {
        if(ex is ServerErrorException) {
            return if(ex.errorCode == ServerCodes.EnterpriseAccountSuspended)
                DevicesResult.RemoveDevice.EnterpriseSuspend()
            else
                DevicesResult.RemoveDevice.Failure(UIMessage(R.string.server_bad_status, arrayOf(ex.errorCode)))
        }
        return DevicesResult.RemoveDevice.Failure(UIMessage(R.string.local_error, arrayOf(ex.toString())))
    }

    override fun work(reporter: ProgressReporter<DevicesResult.RemoveDevice>): DevicesResult.RemoveDevice? {
        val deleteOperation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(deleteOperation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            deleteOperation

        return when (finalResult){
            is Result.Success -> {
                DevicesResult.RemoveDevice.Success(deviceId, position)
            }
            is Result.Failure -> {
                catchException(finalResult.error)
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
                apiClient.token = refreshOperation.value
                workOperation()
            }
            is Result.Failure -> {
                Result.of { throw refreshOperation.error }
            }
        }
    }

}
