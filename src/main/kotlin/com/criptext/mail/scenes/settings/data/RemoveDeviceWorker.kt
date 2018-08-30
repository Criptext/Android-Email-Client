package com.criptext.mail.scenes.settings.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.utils.sha256
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError
import java.io.File



class RemoveDeviceWorker(
        private val password: String,
        private val deviceId: Int,
        private val position: Int,
        httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        override val publishFn: (
                SettingsResult.RemoveDevice) -> Unit)
    : BackgroundWorker<SettingsResult.RemoveDevice> {

    override val canBeParallelized = true
    private val apiClient = SettingsAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): SettingsResult.RemoveDevice {
        return SettingsResult.RemoveDevice.Failure()
    }

    override fun work(reporter: ProgressReporter<SettingsResult.RemoveDevice>): SettingsResult.RemoveDevice? {
        val deleteOperation = Result.of {apiClient.deleteDevice(deviceId, password.sha256())}
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when (deleteOperation){
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

}
