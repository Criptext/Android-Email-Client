package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.ServerErrorCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.sha256
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import java.io.File

class DeleteAccountWorker(private val db: EventLocalDB,
                          private val password: String,
                          private val storage: KeyValueStorage,
                          httpClient: HttpClient,
                          private val activeAccount: ActiveAccount,
                          override val publishFn: (
                                GeneralResult.DeleteAccount) -> Unit)
                    : BackgroundWorker<GeneralResult.DeleteAccount> {

    override val canBeParallelized = true
    private val apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): GeneralResult.DeleteAccount {
        return GeneralResult.DeleteAccount.Failure(createErrorMessage(ex))
    }

    override fun work(reporter: ProgressReporter<GeneralResult.DeleteAccount>): GeneralResult.DeleteAccount? {
        val deleteOperation = Result.of {apiClient.deleteAccount(password.sha256())}
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
                .flatMap { Result.of {
                        db.logoutNukeDB()
                } }
                .flatMap {
                    Result.of {
                        storage.clearAll()
                    }
                }
        return when (deleteOperation){
            is Result.Success -> {
                GeneralResult.DeleteAccount.Success()
            }
            is Result.Failure -> {
                catchException(deleteOperation.error)
            }
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is ServerErrorException -> {
                when {
                    ex.errorCode == ServerErrorCodes.BadRequest -> UIMessage(resId = R.string.password_enter_error)
                    else -> UIMessage(resId = R.string.server_error_exception)
                }
            }
            else -> UIMessage(resId = R.string.server_error_exception)
        }
    }

}
