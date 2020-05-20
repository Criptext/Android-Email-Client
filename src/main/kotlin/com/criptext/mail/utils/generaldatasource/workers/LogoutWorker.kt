package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.services.jobs.CloudBackupJobService
import com.criptext.mail.utils.AccountUtils
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError


/**
 * Created by danieltigse on 28/06/18.
 */

class LogoutWorker(
        private val shouldDeleteAllData: Boolean,
        private val letAPIKnow: Boolean,
        private val db: EventLocalDB,
        private val accountDao: AccountDao,
        private val storage: KeyValueStorage,
        private val httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        override val publishFn: (
                GeneralResult.Logout) -> Unit)
    : BackgroundWorker<GeneralResult.Logout> {

    override val canBeParallelized = true
    private val apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)
    private var newActiveAccount: ActiveAccount? = null

    override fun catchException(ex: Exception): GeneralResult.Logout {
        return GeneralResult.Logout.Failure()
    }

    override fun work(reporter: ProgressReporter<GeneralResult.Logout>): GeneralResult.Logout? {
        val deleteOperation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(deleteOperation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            deleteOperation

        return when (finalResult){
            is Result.Success -> {
                GeneralResult.Logout.Success(newActiveAccount, activeAccount.userEmail, activeAccount.id)
            }
            is Result.Failure -> {
                GeneralResult.Logout.Failure()
            }
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private fun workOperation() : Result<Unit, Exception> = if(letAPIKnow) {
        Result.of {apiClient.postLogout()}
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
                .flatMap {
                    localLogout()
                }
        } else localLogout()

    private fun localLogout(): Result<Unit, Exception> = Result.of {
        if(shouldDeleteAllData)
            db.logoutNukeDB(activeAccount)
        else
            db.logout(activeAccount.id)
    }
    .flatMap { Result.of {
        CloudBackupJobService.cancelJob(storage, activeAccount.id)
    } }
    .flatMap {
        Result.of {
            val accounts = db.getLoggedAccounts()
            if (accounts.isNotEmpty()) {
                db.setActiveAccount(accounts.first().id)
                newActiveAccount = AccountUtils.setUserAsActiveAccount(accounts.first(), storage)
            } else {
                storage.clearAll()
            }
            if (!shouldDeleteAllData) {
                val loggedOutAccounts = AccountUtils.getLastLoggedAccounts(storage)
                loggedOutAccounts.add(activeAccount.userEmail)
                storage.putString(KeyValueStorage.StringKey.LastLoggedUser, loggedOutAccounts.distinct().joinToString())
            }
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

}
