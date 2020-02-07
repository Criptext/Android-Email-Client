package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.services.data.JobIdData
import com.criptext.mail.services.jobs.CloudBackupJobService
import com.criptext.mail.utils.AccountUtils
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.sha256
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError

class DeleteAccountWorker(private val db: EventLocalDB,
                          private val accountDao: AccountDao,
                          private val password: String,
                          private val storage: KeyValueStorage,
                          private val httpClient: HttpClient,
                          private val activeAccount: ActiveAccount,
                          override val publishFn: (
                                GeneralResult.DeleteAccount) -> Unit)
                    : BackgroundWorker<GeneralResult.DeleteAccount> {

    override val canBeParallelized = true
    private val apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)
    private var newActiveAccount: ActiveAccount? = null

    override fun catchException(ex: Exception): GeneralResult.DeleteAccount {
        return GeneralResult.DeleteAccount.Failure(createErrorMessage(ex))
    }

    override fun work(reporter: ProgressReporter<GeneralResult.DeleteAccount>): GeneralResult.DeleteAccount? {
        val deleteOperation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(deleteOperation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            deleteOperation

        val accounts = db.getLoggedAccounts()
        if(accounts.isNotEmpty()){
            db.setActiveAccount(accounts.first().id)
            newActiveAccount = AccountUtils.setUserAsActiveAccount(accounts.first(), storage)
        }

        return when (finalResult){
            is Result.Success -> {
                GeneralResult.DeleteAccount.Success(newActiveAccount)
            }
            is Result.Failure -> {
                catchException(finalResult.error)
            }
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private fun workOperation() : Result<Unit, Exception> = Result.of {apiClient.deleteAccount(password.sha256())}
            .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
            .flatMap { Result.of {
                db.logoutNukeDB(activeAccount)
            } }
            .flatMap {
                Result.of {
                    CloudBackupJobService.cancelJob(storage, activeAccount.id)
                }
            }

    private fun newRetryWithNewSessionOperation()
            : Result<Unit, Exception> {
        val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient,
                activeAccount, storage, accountDao)
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

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is ServerErrorException -> {
                when {
                    ex.errorCode == ServerCodes.BadRequest -> UIMessage(resId = R.string.password_enter_error)
                    else -> UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
                }
            }
            else -> UIMessage(resId = R.string.server_error_exception)
        }
    }

}
