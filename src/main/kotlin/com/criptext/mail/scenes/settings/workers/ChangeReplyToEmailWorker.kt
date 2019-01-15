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
import com.criptext.mail.scenes.settings.data.SettingsResult
import com.criptext.mail.scenes.settings.recovery_email.data.RecoveryEmailAPIClient
import com.criptext.mail.scenes.settings.recovery_email.data.RecoveryEmailResult
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.sha256
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError


/**
 * Created by danieltigse on 28/06/18.
 */

class ChangeReplyToEmailWorker(
        private val newEmail: String,
        private val storage: KeyValueStorage,
        private val accountDao: AccountDao,
        private val httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        override val publishFn: (
                SettingsResult.SetReplyToEmail) -> Unit)
    : BackgroundWorker<SettingsResult.SetReplyToEmail> {

    override val canBeParallelized = true
    private val apiClient = RecoveryEmailAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): SettingsResult.SetReplyToEmail {
        return if(ex is ServerErrorException) {
            when(ex.errorCode) {
                ServerCodes.MethodNotAllowed ->
                    SettingsResult.SetReplyToEmail.Failure(UIMessage(R.string.recovery_email_change_fail_same))
                ServerCodes.BadRequest -> SettingsResult.SetReplyToEmail.Failure(UIMessage(R.string.password_enter_error))
                else -> SettingsResult.SetReplyToEmail.Failure(UIMessage(R.string.server_error_exception))
            }
        }else {
            SettingsResult.SetReplyToEmail.Failure(UIMessage(R.string.server_error_exception))
        }
    }

    override fun work(reporter: ProgressReporter<SettingsResult.SetReplyToEmail>): SettingsResult.SetReplyToEmail? {
        val changeEmailOperation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(changeEmailOperation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            changeEmailOperation

        return when (finalResult){
            is Result.Success -> {
                SettingsResult.SetReplyToEmail.Success(newEmail)
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
        apiClient.putChangeReplyToEmail(newEmail).body
    }
    .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)

    private fun newRetryWithNewSessionOperation()
            : Result<String, Exception> {
        val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient, activeAccount, storage, accountDao)
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

}
