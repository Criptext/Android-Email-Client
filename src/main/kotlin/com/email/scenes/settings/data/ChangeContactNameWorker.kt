package com.email.scenes.settings.data

import com.email.api.HttpClient
import com.email.api.HttpErrorHandlingHelper
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.SettingsLocalDB
import com.email.db.models.ActiveAccount
import com.email.db.models.Contact
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError

class ChangeContactNameWorker(
        private val fullName: String,
        private val recipientId: String,
        private val settingsLocalDB: SettingsLocalDB,
        httpClient: HttpClient,
        activeAccount: ActiveAccount,
        override val publishFn: (SettingsResult.ChangeContactName) -> Unit)
    : BackgroundWorker<SettingsResult.ChangeContactName> {

    private val apiClient = SettingsAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): SettingsResult.ChangeContactName {
        return SettingsResult.ChangeContactName.Failure()
    }

    override fun work(reporter: ProgressReporter<SettingsResult.ChangeContactName>): SettingsResult.ChangeContactName? {
        val result = Result.of { apiClient.putUsernameChange(fullName)
            apiClient.postUsernameChangedEvent(fullName) }
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when (result){
            is Result.Success -> {
                settingsLocalDB.contactDao.updateContactName("$recipientId@${Contact.mainDomain}", fullName)
                settingsLocalDB.accountDao.updateProfileName(fullName, recipientId)

                SettingsResult.ChangeContactName.Success(fullName)
            }
            is Result.Failure -> {
                catchException(result.error)
            }
        }
    }

    override fun cancel() {
    }

}