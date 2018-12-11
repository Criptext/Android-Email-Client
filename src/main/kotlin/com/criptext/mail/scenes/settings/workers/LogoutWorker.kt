package com.criptext.mail.scenes.settings.workers

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.data.SettingsAPIClient
import com.criptext.mail.scenes.settings.data.SettingsResult
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError


/**
 * Created by danieltigse on 28/06/18.
 */

class LogoutWorker(
        private val db: SettingsLocalDB,
        private val storage: KeyValueStorage,
        private val httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        override val publishFn: (
                SettingsResult.Logout) -> Unit)
    : BackgroundWorker<SettingsResult.Logout> {

    override val canBeParallelized = true
    private val apiClient = SettingsAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): SettingsResult.Logout {
        return SettingsResult.Logout.Failure()
    }

    override fun work(reporter: ProgressReporter<SettingsResult.Logout>): SettingsResult.Logout? {
        val deleteOperation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(deleteOperation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            deleteOperation

        return when (finalResult){
            is Result.Success -> {
                SettingsResult.Logout.Success()
            }
            is Result.Failure -> {
                SettingsResult.Logout.Failure()
            }
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private fun workOperation() : Result<Unit, Exception> = Result.of {apiClient.postLogout()}
            .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
            .flatMap { Result.of { db.logout() } }
            .flatMap {
                Result.of {
                    storage.clearAll()
                    storage.putString(KeyValueStorage.StringKey.LastLoggedUser, activeAccount.recipientId)
                }
            }

    private fun newRetryWithNewSessionOperation()
            : Result<Unit, Exception> {
        val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient,
                activeAccount, storage, db.accountDao)
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
