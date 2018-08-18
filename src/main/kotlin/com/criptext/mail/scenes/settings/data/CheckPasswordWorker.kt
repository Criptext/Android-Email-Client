package com.criptext.mail.scenes.settings.data

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.devices.DeviceItem
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.sha256
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError
import kotlinx.android.synthetic.main.activity_form_signup.view.*
import java.io.File



/**
 * Created by danieltigse on 28/06/18.
 */

class CheckPasswordWorker(
        private val password: String,
        httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        override val publishFn: (
                SettingsResult.CheckPassword) -> Unit)
    : BackgroundWorker<SettingsResult.CheckPassword> {

    override val canBeParallelized = true
    private val apiClient = SettingsAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): SettingsResult.CheckPassword {
        return SettingsResult.CheckPassword.Failure(UIMessage(R.string.password_enter_error), ex)
    }

    override fun work(reporter: ProgressReporter<SettingsResult.CheckPassword>): SettingsResult.CheckPassword? {
        val checkPasswordOperation =
                Result.of {
                    apiClient.checkPassword(activeAccount.recipientId, password.sha256())
                }
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when (checkPasswordOperation){
            is Result.Success -> {
                SettingsResult.CheckPassword.Success()
            }
            is Result.Failure -> {
                catchException(checkPasswordOperation.error)
            }
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

}
