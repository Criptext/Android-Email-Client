package com.criptext.mail.scenes.settings.data

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.devices.DeviceItem
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError
import java.io.File



/**
 * Created by danieltigse on 28/06/18.
 */

class ListDevicesWorker(
        httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        override val publishFn: (
                SettingsResult.ListDevices) -> Unit)
    : BackgroundWorker<SettingsResult.ListDevices> {

    override val canBeParallelized = true
    private val apiClient = SettingsAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): SettingsResult.ListDevices {
        if(ex is ServerErrorException && ex.errorCode == 401)
            return SettingsResult.ListDevices.Unauthorized(UIMessage(R.string.device_removed_remotely_exception))
        return SettingsResult.ListDevices.Failure(createErrorMessage(ex))
    }

    override fun work(reporter: ProgressReporter<SettingsResult.ListDevices>): SettingsResult.ListDevices? {
        val listDevicesOperation = Result.of {apiClient.listDevices()}
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when (listDevicesOperation){
            is Result.Success -> {
                val devices = DeviceItem.fromJSON(listDevicesOperation.value)
                        .map { if(it.id == activeAccount.deviceId) it.copy(
                                isCurrent = true
                        ) else it }
                SettingsResult.ListDevices.Success(devices)
            }
            is Result.Failure -> {
                catchException(listDevicesOperation.error)
            }
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is ServerErrorException -> UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
            else -> UIMessage(resId = R.string.error_listing_devices)
        }
    }

}
