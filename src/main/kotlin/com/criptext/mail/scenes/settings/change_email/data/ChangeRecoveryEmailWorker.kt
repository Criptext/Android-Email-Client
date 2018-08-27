package com.criptext.mail.scenes.settings.change_email.data

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
import kotlinx.android.synthetic.main.activity_form_signup.view.*


/**
 * Created by danieltigse on 28/06/18.
 */

class ChangeRecoveryEmailWorker(
        private val password: String,
        private val newEmail: String,
        httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        override val publishFn: (
                ChangeEmailResult.ChangeRecoveryEmail) -> Unit)
    : BackgroundWorker<ChangeEmailResult.ChangeRecoveryEmail> {

    override val canBeParallelized = true
    private val apiClient = ChangeEmailAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): ChangeEmailResult.ChangeRecoveryEmail {
        return ChangeEmailResult.ChangeRecoveryEmail.Failure(UIMessage(R.string.password_enter_error))
    }

    override fun work(reporter: ProgressReporter<ChangeEmailResult.ChangeRecoveryEmail>): ChangeEmailResult.ChangeRecoveryEmail? {
        val changeEmailOperation =
                Result.of {
                    apiClient.putChangerecoveryEmail(newEmail, password.sha256())
                }
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when (changeEmailOperation){
            is Result.Success -> {
                ChangeEmailResult.ChangeRecoveryEmail.Success(newEmail)
            }
            is Result.Failure -> {
                catchException(changeEmailOperation.error)
            }
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

}
