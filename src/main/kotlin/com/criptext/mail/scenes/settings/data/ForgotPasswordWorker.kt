package com.criptext.mail.scenes.settings.data

import android.accounts.NetworkErrorException
import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.signup.data.SignUpAPIClient
import com.criptext.mail.scenes.signup.data.SignUpResult
import com.criptext.mail.utils.ServerErrorCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import org.json.JSONException


class ForgotPasswordWorker(val httpClient: HttpClient,
                           val activeAccount: ActiveAccount,
                           override val publishFn: (SettingsResult) -> Unit)
    : BackgroundWorker<SettingsResult.ResetPassword> {

    private val apiClient = SettingsAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = true

    override fun catchException(ex: Exception): SettingsResult.ResetPassword {
        return SettingsResult.ResetPassword.Failure(createErrorMessage(ex))
    }

    override fun work(reporter: ProgressReporter<SettingsResult.ResetPassword>): SettingsResult.ResetPassword? {
        val result = Result.of { apiClient.postForgotPassword(activeAccount.recipientId) }

        return when (result) {
            is Result.Success -> SettingsResult.ResetPassword.Success()
            is Result.Failure -> catchException(result.error)
        }

    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is ServerErrorException ->
                if(ex.errorCode == ServerErrorCodes.BadRequest)
                UIMessage(resId = R.string.forgot_password_error_400)
                else
                    UIMessage(resId = R.string.forgot_password_error)
            else ->UIMessage(resId = R.string.forgot_password_error)
        }
    }

}