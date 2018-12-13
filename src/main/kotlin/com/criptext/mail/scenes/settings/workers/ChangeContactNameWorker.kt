package com.criptext.mail.scenes.settings.workers

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.scenes.settings.data.SettingsAPIClient
import com.criptext.mail.scenes.settings.data.SettingsResult
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError

class ChangeContactNameWorker(
        private val fullName: String,
        private val recipientId: String,
        private val settingsLocalDB: SettingsLocalDB,
        private val httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        override val publishFn: (SettingsResult.ChangeContactName) -> Unit)
    : BackgroundWorker<SettingsResult.ChangeContactName> {

    private val apiClient = SettingsAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): SettingsResult.ChangeContactName {
        return SettingsResult.ChangeContactName.Failure()
    }

    override fun work(reporter: ProgressReporter<SettingsResult.ChangeContactName>): SettingsResult.ChangeContactName? {
        val result = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(result)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            result

        return when (finalResult){
            is Result.Success -> {
                settingsLocalDB.contactDao.updateContactName("$recipientId@${Contact.mainDomain}", fullName)
                settingsLocalDB.accountDao.updateProfileName(fullName, recipientId)

                SettingsResult.ChangeContactName.Success(fullName)
            }
            is Result.Failure -> {
                catchException(finalResult.error)
            }
        }
    }

    override fun cancel() {
    }

    private fun workOperation() : Result<String, Exception> = Result.of { apiClient.putUsernameChange(fullName)
    }.mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)

    private fun newRetryWithNewSessionOperation()
            : Result<String, Exception> {
        val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient, activeAccount, storage, settingsLocalDB.accountDao)
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