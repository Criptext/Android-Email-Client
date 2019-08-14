package com.criptext.mail.utils.generaldatasource.workers

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
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError


class TwoFAWorker(val httpClient: HttpClient,
                  val activeAccount: ActiveAccount,
                  private val storage: KeyValueStorage,
                  private val accountDao: AccountDao,
                  private val twoFA: Boolean,
                  override val publishFn: (GeneralResult) -> Unit)
    : BackgroundWorker<GeneralResult.Set2FA> {

    private val apiClient = SettingsAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): GeneralResult.Set2FA {
        return if(ex is ServerErrorException) {
            when(ex.errorCode) {
                ServerCodes.MethodNotAllowed -> GeneralResult.Set2FA.Failure(UIMessage(R.string.message_warning_two_fa), twoFA)
                else -> GeneralResult.Set2FA.Failure(UIMessage(R.string.server_bad_status, arrayOf(ex.errorCode)), twoFA)
            }
        }else {
            GeneralResult.Set2FA.Failure(UIMessage(R.string.unknown_error, arrayOf(ex.toString())), twoFA)
        }
    }

    override fun work(reporter: ProgressReporter<GeneralResult.Set2FA>): GeneralResult.Set2FA? {
        val result =  workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(result)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            result

        return when (finalResult) {
            is Result.Success -> GeneralResult.Set2FA.Success(twoFA)

            is Result.Failure -> catchException(finalResult.error)
        }
    }

    private fun workOperation() : Result<String, Exception> = Result.of {
        apiClient.putTwoFA(twoFA).body
    }

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