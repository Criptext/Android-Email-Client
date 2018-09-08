package com.criptext.mail.scenes.settings.changepassword.data

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.sha256
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError

class ChangePasswordWorker(
        private val oldPassword: String,
        private val password: String,
        httpClient: HttpClient,
        activeAccount: ActiveAccount,
        override val publishFn: (
                ChangePasswordResult.ChangePassword) -> Unit)
    : BackgroundWorker<ChangePasswordResult.ChangePassword> {

    override val canBeParallelized = true
    private val apiClient = ChangePasswordAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): ChangePasswordResult.ChangePassword {
        return ChangePasswordResult.ChangePassword.Failure(UIMessage(R.string.password_enter_error), ex)
    }

    override fun work(reporter: ProgressReporter<ChangePasswordResult.ChangePassword>): ChangePasswordResult.ChangePassword? {
        val checkPasswordOperation =
                Result.of {
                    apiClient.putChangePassword(oldPassword.sha256(), password.sha256())
                }
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when (checkPasswordOperation){
            is Result.Success -> {
                ChangePasswordResult.ChangePassword.Success()
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
