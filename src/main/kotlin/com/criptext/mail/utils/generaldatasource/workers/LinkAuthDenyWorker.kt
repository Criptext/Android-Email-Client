package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError

class LinkAuthDenyWorker(private val untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo,
                         private var activeAccount: ActiveAccount,
                         private val httpClient: HttpClient,
                         private val accountDao: AccountDao,
                         private val storage: KeyValueStorage,
                         override val publishFn: (GeneralResult.LinkDeny) -> Unit
                          ) : BackgroundWorker<GeneralResult.LinkDeny> {

    override val canBeParallelized = false

    private var apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): GeneralResult.LinkDeny {
        if(ex is ServerErrorException)
            return GeneralResult.LinkDeny.Failure(UIMessage(R.string.server_bad_status, arrayOf(ex.errorCode)))
        return GeneralResult.LinkDeny.Failure(UIMessage(R.string.server_error_exception))
    }

    override fun work(reporter: ProgressReporter<GeneralResult.LinkDeny>)
            : GeneralResult.LinkDeny? {

        val account = accountDao.getAccount(untrustedDeviceInfo.recipientId, untrustedDeviceInfo.domain) ?: return GeneralResult.LinkDeny.Failure(UIMessage(R.string.server_error_exception))
        if(account.recipientId.plus("@${account.domain}") != activeAccount.userEmail) setup(account)

        val operation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(operation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            operation

        return when (finalResult){
            is Result.Success -> {
                GeneralResult.LinkDeny.Success()
            }
            is Result.Failure -> {
                catchException(finalResult.error)
            }
        }
    }

    override fun cancel() {
        TODO("not implemented")
    }

    private fun setup(account: Account){
        activeAccount = ActiveAccount.loadFromDB(account)!!
        apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)
    }


    private fun workOperation() : Result<String, Exception> = Result.of {
        apiClient.postLinkDeny(untrustedDeviceInfo.deviceId).body
    }

    private fun newRetryWithNewSessionOperation()
            : Result<String, Exception> {
        val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient,
                activeAccount, storage, accountDao)
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when(refreshOperation){
            is Result.Success -> {
                apiClient.token = refreshOperation.value
                workOperation()
            }
            is Result.Failure -> {
                Result.of { throw refreshOperation.error }
            }
        }
    }
}