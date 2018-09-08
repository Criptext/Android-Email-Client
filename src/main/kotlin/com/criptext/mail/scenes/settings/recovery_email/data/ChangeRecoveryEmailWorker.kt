package com.criptext.mail.scenes.settings.recovery_email.data

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


/**
 * Created by danieltigse on 28/06/18.
 */

class ChangeRecoveryEmailWorker(
        private val password: String,
        private val newEmail: String,
        httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        override val publishFn: (
                RecoveryEmailResult.ChangeRecoveryEmail) -> Unit)
    : BackgroundWorker<RecoveryEmailResult.ChangeRecoveryEmail> {

    override val canBeParallelized = true
    private val apiClient = RecoveryEmailAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): RecoveryEmailResult.ChangeRecoveryEmail {
        return RecoveryEmailResult.ChangeRecoveryEmail.Failure(UIMessage(R.string.password_enter_error))
    }

    override fun work(reporter: ProgressReporter<RecoveryEmailResult.ChangeRecoveryEmail>): RecoveryEmailResult.ChangeRecoveryEmail? {
        val changeEmailOperation =
                Result.of {
                    apiClient.putChangerecoveryEmail(newEmail, password.sha256())
                }
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when (changeEmailOperation){
            is Result.Success -> {
                RecoveryEmailResult.ChangeRecoveryEmail.Success(newEmail)
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
