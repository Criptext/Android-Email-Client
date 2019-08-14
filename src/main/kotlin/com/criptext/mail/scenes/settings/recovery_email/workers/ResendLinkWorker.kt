package com.criptext.mail.scenes.settings.recovery_email.workers

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
import com.criptext.mail.scenes.settings.recovery_email.data.RecoveryEmailResult
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError
import kotlinx.android.synthetic.main.restore_backup_dialog.view.*


/**
 * Created by danieltigse on 28/06/18.
 */

class ResendLinkWorker(
        private val storage: KeyValueStorage,
        private val accountDao: AccountDao,
        private val httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        override val publishFn: (
                RecoveryEmailResult.ResendConfirmationLink) -> Unit)
    : BackgroundWorker<RecoveryEmailResult.ResendConfirmationLink> {

    override val canBeParallelized = true
    private val apiClient = RecoveryEmailAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): RecoveryEmailResult.ResendConfirmationLink {
        if(ex is ServerErrorException) RecoveryEmailResult.ResendConfirmationLink.Failure(UIMessage(R.string.server_bad_status, arrayOf(ex.errorCode)))
        return RecoveryEmailResult.ResendConfirmationLink.Failure(UIMessage(R.string.local_error, arrayOf(ex.toString())))
    }

    override fun work(reporter: ProgressReporter<RecoveryEmailResult.ResendConfirmationLink>): RecoveryEmailResult.ResendConfirmationLink? {
        val operation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(operation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            operation

        return when (finalResult){
            is Result.Success -> {
                RecoveryEmailResult.ResendConfirmationLink.Success()
            }
            is Result.Failure -> {
                catchException(finalResult.error)
            }
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private fun workOperation() : Result<String, Exception> = Result.of {apiClient.putResendLink().body}
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
