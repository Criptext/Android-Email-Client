package com.criptext.mail.scenes.settings.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.data.SettingsAPIClient
import com.criptext.mail.scenes.settings.data.SettingsResult
import com.criptext.mail.utils.ServerErrorCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError


class TwoFAWorker(val httpClient: HttpClient,
                  val activeAccount: ActiveAccount,
                  private val storage: KeyValueStorage,
                  private val accountDao: AccountDao,
                  private val twoFA: Boolean,
                  override val publishFn: (SettingsResult) -> Unit)
    : BackgroundWorker<SettingsResult.Set2FA> {

    private val apiClient = SettingsAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): SettingsResult.Set2FA {
        return if(ex is ServerErrorException) {
            when(ex.errorCode) {
                ServerErrorCodes.MethodNotAllowed -> SettingsResult.Set2FA.Failure(UIMessage(R.string.message_warning_two_fa), twoFA)
                else -> SettingsResult.Set2FA.Failure(UIMessage(R.string.server_error_exception), twoFA)
            }
        }else {
            SettingsResult.Set2FA.Failure(UIMessage(R.string.server_error_exception), twoFA)
        }
    }

    override fun work(reporter: ProgressReporter<SettingsResult.Set2FA>): SettingsResult.Set2FA? {
        val result =  workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(result)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            result

        return when (finalResult) {
            is Result.Success -> SettingsResult.Set2FA.Success(twoFA)

            is Result.Failure -> catchException(finalResult.error)
        }
    }

    private fun workOperation() : Result<String, Exception> = Result.of { apiClient.putTwoFA(twoFA) }

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

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }
}