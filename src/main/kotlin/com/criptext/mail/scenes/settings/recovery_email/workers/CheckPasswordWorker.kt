package com.criptext.mail.scenes.settings.recovery_email.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.recovery_email.data.RecoveryEmailAPIClient
import com.criptext.mail.scenes.settings.recovery_email.data.RecoveryEmailResult
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.sha256
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError


/**
 * Created by danieltigse on 28/06/18.
 */

class CheckPasswordWorker(
        private val password: String,
        private val storage: KeyValueStorage,
        private val accountDao: AccountDao,
        private val httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        override val publishFn: (
                RecoveryEmailResult.CheckPassword) -> Unit)
    : BackgroundWorker<RecoveryEmailResult.CheckPassword> {

    override val canBeParallelized = true
    private val apiClient = RecoveryEmailAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): RecoveryEmailResult.CheckPassword {
        return RecoveryEmailResult.CheckPassword.Failure(UIMessage(R.string.password_enter_error))
    }

    override fun work(reporter: ProgressReporter<RecoveryEmailResult.CheckPassword>): RecoveryEmailResult.CheckPassword? {
        val checkPasswordOperation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(checkPasswordOperation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            checkPasswordOperation

        return when (finalResult){
            is Result.Success -> {
                RecoveryEmailResult.CheckPassword.Success()
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
        apiClient.checkPassword(activeAccount.recipientId, password.sha256())
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
