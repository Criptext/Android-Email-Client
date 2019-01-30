package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.scenes.settings.data.SettingsAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError

class ChangeContactNameWorker(
        private val fullName: String,
        private val recipientId: String,
        private val db: AppDatabase,
        private val httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        override val publishFn: (GeneralResult.ChangeContactName) -> Unit)
    : BackgroundWorker<GeneralResult.ChangeContactName> {

    private val apiClient = SettingsAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): GeneralResult.ChangeContactName {
        return GeneralResult.ChangeContactName.Failure()
    }

    override fun work(reporter: ProgressReporter<GeneralResult.ChangeContactName>): GeneralResult.ChangeContactName? {
        val result = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(result)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            result

        return when (finalResult){
            is Result.Success -> {
                db.contactDao().updateContactName("$recipientId@${Contact.mainDomain}", fullName)
                db.accountDao().updateProfileName(fullName, recipientId)

                GeneralResult.ChangeContactName.Success(fullName)
            }
            is Result.Failure -> {
                catchException(finalResult.error)
            }
        }
    }

    override fun cancel() {
    }

    private fun workOperation() : Result<String, Exception> = Result.of {
        apiClient.putUsernameChange(fullName).body
    }.mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)

    private fun newRetryWithNewSessionOperation()
            : Result<String, Exception> {
        val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient, activeAccount, storage, db.accountDao())
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