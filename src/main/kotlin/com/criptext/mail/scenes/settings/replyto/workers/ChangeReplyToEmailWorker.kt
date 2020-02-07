package com.criptext.mail.scenes.settings.replyto.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.recovery_email.data.RecoveryEmailAPIClient
import com.criptext.mail.scenes.settings.replyto.data.ReplyToResult
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError


class ChangeReplyToEmailWorker(
        private val newEmail: String,
        private val enabled: Boolean,
        private val storage: KeyValueStorage,
        private val accountDao: AccountDao,
        private val httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        override val publishFn: (
                ReplyToResult.SetReplyToEmail) -> Unit)
    : BackgroundWorker<ReplyToResult.SetReplyToEmail> {

    override val canBeParallelized = true
    private val apiClient = RecoveryEmailAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): ReplyToResult.SetReplyToEmail {
        return if(ex is ServerErrorException) {
            when(ex.errorCode) {
                ServerCodes.MethodNotAllowed ->
                    ReplyToResult.SetReplyToEmail.Failure(UIMessage(R.string.recovery_email_change_fail_same))
                ServerCodes.BadRequest -> ReplyToResult.SetReplyToEmail.Failure(UIMessage(R.string.password_enter_error))
                ServerCodes.Forbidden -> ReplyToResult.SetReplyToEmail.Forbidden()
                ServerCodes.EnterpriseAccountSuspended -> ReplyToResult.SetReplyToEmail.EnterpriseSuspended()
                else -> ReplyToResult.SetReplyToEmail.Failure(UIMessage(R.string.server_bad_status, arrayOf(ex.errorCode)))
            }
        }else {
            ReplyToResult.SetReplyToEmail.Failure(UIMessage(R.string.server_error_exception))
        }
    }

    override fun work(reporter: ProgressReporter<ReplyToResult.SetReplyToEmail>): ReplyToResult.SetReplyToEmail? {
        val changeEmailOperation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(changeEmailOperation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            changeEmailOperation

        return when (finalResult){
            is Result.Success -> {
                ReplyToResult.SetReplyToEmail.Success(newEmail, enabled)
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
        apiClient.putChangeReplyToEmail(newEmail, enabled).body
    }
    .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)

    private fun newRetryWithNewSessionOperation()
            : Result<String, Exception> {
        val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient, activeAccount, storage, accountDao)
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
