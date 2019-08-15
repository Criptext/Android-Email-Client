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
import com.criptext.mail.signal.PreKeyBundleShareData
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONObject


class CheckForKeyBundleWorker(
        private val deviceId: Int,
        private val accountDao: AccountDao,
        private val storage: KeyValueStorage,
        private val httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        override val publishFn: (
                GeneralResult.CheckForKeyBundle) -> Unit)
    : BackgroundWorker<GeneralResult.CheckForKeyBundle> {

    override val canBeParallelized = true
    private val apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): GeneralResult.CheckForKeyBundle {
        if(ex is ServerErrorException)
            return GeneralResult.CheckForKeyBundle.Failure(UIMessage(R.string.server_bad_status, arrayOf(ex.errorCode)))
        return GeneralResult.CheckForKeyBundle.Failure(UIMessage(R.string.keybundle_get_error, arrayOf(ex.toString())))
    }

    override fun work(reporter: ProgressReporter<GeneralResult.CheckForKeyBundle>): GeneralResult.CheckForKeyBundle? {
        val operation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(operation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            operation

        return when (finalResult){
            is Result.Success -> {
                GeneralResult.CheckForKeyBundle.Success(finalResult.value)
            }
            is Result.Failure -> {
                catchException(finalResult.error)
            }
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private fun workOperation() : Result<PreKeyBundleShareData.DownloadBundle, Exception> = Result.of {
        apiClient.getKeyBundle(deviceId)
    }
    .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
    .flatMap { Result.of {
        PreKeyBundleShareData.DownloadBundle.fromJSON(JSONObject(it.body), activeAccount.id)
    } }

    private fun newRetryWithNewSessionOperation()
            : Result<PreKeyBundleShareData.DownloadBundle, Exception> {
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

}
