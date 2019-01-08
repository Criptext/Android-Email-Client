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
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError


class ReadReceiptsWorker(val httpClient: HttpClient,
                         val activeAccount: ActiveAccount,
                         private val readReceipts: Boolean,
                         private val storage: KeyValueStorage,
                         private val accountDao: AccountDao,
                         override val publishFn: (GeneralResult) -> Unit)
    : BackgroundWorker<GeneralResult.SetReadReceipts> {

    private val apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): GeneralResult.SetReadReceipts {
        return if(ex is ServerErrorException) {
            when(ex.errorCode) {
                ServerCodes.MethodNotAllowed ->
                    GeneralResult.SetReadReceipts.Failure(UIMessage(R.string.message_warning_two_fa), readReceipts)
                else -> GeneralResult.SetReadReceipts.Failure(UIMessage(R.string.server_error_exception), readReceipts)
            }
        }else {
            GeneralResult.SetReadReceipts.Failure(UIMessage(R.string.server_error_exception), readReceipts)
        }
    }

    override fun work(reporter: ProgressReporter<GeneralResult.SetReadReceipts>): GeneralResult.SetReadReceipts? {
        val result = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(result)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            result

        return when (finalResult) {
            is Result.Success -> GeneralResult.SetReadReceipts.Success(readReceipts)

            is Result.Failure -> catchException(finalResult.error)
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    private fun workOperation() : Result<String, Exception> = Result.of { apiClient.putReadReceipts(readReceipts).body }

    private fun newRetryWithNewSessionOperation()
            : Result<String, Exception> {
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