package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONObject


class SyncStatusWorker(val httpClient: HttpClient,
                       private val activeAccount: ActiveAccount,
                       private val storage: KeyValueStorage,
                       private val accountDao: AccountDao,
                       override val publishFn: (GeneralResult) -> Unit)
    : BackgroundWorker<GeneralResult.SyncStatus> {

    private val apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): GeneralResult.SyncStatus {
        when(ex){
            is ServerErrorException -> {
                when(ex.errorCode){
                    ServerCodes.AuthenticationPending -> return GeneralResult.SyncStatus.Waiting()
                    ServerCodes.AuthenticationDenied -> return GeneralResult.SyncStatus.Denied()
                }
            }
        }
        return GeneralResult.SyncStatus.Waiting()
    }

    override fun work(reporter: ProgressReporter<GeneralResult.SyncStatus>): GeneralResult.SyncStatus? {
        val result = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(result)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            result

        return when (finalResult) {
            is Result.Success ->{
                GeneralResult.SyncStatus.Success(finalResult.value)
            }
            is Result.Failure -> catchException(finalResult.error)
        }
    }

    private fun workOperation() : Result<SyncStatusData, Exception> = Result.of { apiClient.getSyncStatus() }
            .flatMap { Result.of { SyncStatusData.fromJSON(it.body) } }
            .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)

    private fun newRetryWithNewSessionOperation()
            : Result<SyncStatusData, Exception> {
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

    private val createErrorMessage: (ex: Exception) -> UIMessage = { _ ->
        UIMessage(resId = R.string.forgot_password_error)
    }

}