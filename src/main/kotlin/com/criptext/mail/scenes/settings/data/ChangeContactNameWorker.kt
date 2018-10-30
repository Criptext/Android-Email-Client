package com.criptext.mail.scenes.settings.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
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
            }.mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
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