package com.criptext.mail.scenes.settings.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import java.io.File



/**
 * Created by danieltigse on 28/06/18.
 */

class LogoutWorker(
        private val db: SettingsLocalDB,
        private val storage: KeyValueStorage,
        httpClient: HttpClient,
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
        val deleteOperation = Result.of {apiClient.postLogout()}
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
                .flatMap { Result.Companion.of { db.logout() } }
                .flatMap { Result.Companion.of { storage.putString(KeyValueStorage.StringKey.LastLoggedUser, activeAccount.recipientId) } }
        return when (deleteOperation){
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

}
