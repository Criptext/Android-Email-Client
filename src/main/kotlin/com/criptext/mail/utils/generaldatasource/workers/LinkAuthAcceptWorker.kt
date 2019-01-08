package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError
import org.json.JSONObject

class LinkAuthAcceptWorker(private val untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo,
                           private val activeAccount: ActiveAccount,
                           private val httpClient: HttpClient,
                           private val accountDao: AccountDao,
                           private val storage: KeyValueStorage,
                           override val publishFn: (GeneralResult.LinkAccept) -> Unit
                          ) : BackgroundWorker<GeneralResult.LinkAccept> {

    override val canBeParallelized = false

    private val apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): GeneralResult.LinkAccept {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(reporter: ProgressReporter<GeneralResult.LinkAccept>)
            : GeneralResult.LinkAccept? {
        if(untrustedDeviceInfo.recipientId != activeAccount.recipientId)
            return GeneralResult.LinkAccept.Failure(UIMessage(R.string.server_error_exception))

        val operation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(operation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            operation

        return when (finalResult){
            is Result.Success -> {
                GeneralResult.LinkAccept.Success(finalResult.value, untrustedDeviceInfo.deviceId, untrustedDeviceInfo.deviceType)
            }
            is Result.Failure -> {
                GeneralResult.LinkAccept.Failure(UIMessage(R.string.server_error_exception))
            }
        }
    }

    override fun cancel() {
        TODO("not implemented")
    }

    private fun workOperation() : Result<Int, Exception> = Result.of {
        JSONObject(apiClient.postLinkAccept(untrustedDeviceInfo.deviceId).body).getInt("deviceId")
    }

    private fun newRetryWithNewSessionOperation()
            : Result<Int, Exception> {
        val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient,
                activeAccount, storage, accountDao)
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