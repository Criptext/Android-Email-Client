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
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.sha256
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError

class ConfirmPasswordWorker(private val password: String,
                            private val activeAccount: ActiveAccount,
                            private val httpClient: HttpClient,
                            private val storage: KeyValueStorage,
                            private val accountDao: AccountDao,
                            override val publishFn: (GeneralResult.ConfirmPassword) -> Unit
                          ) : BackgroundWorker<GeneralResult.ConfirmPassword> {

    override val canBeParallelized = false

    private val apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): GeneralResult.ConfirmPassword {
        val message = createErrorMessage(ex)
        return GeneralResult.ConfirmPassword.Failure(message)
    }

    override fun work(reporter: ProgressReporter<GeneralResult.ConfirmPassword>)
            : GeneralResult.ConfirmPassword? {

        val confirmPasswordOperation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(confirmPasswordOperation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            confirmPasswordOperation

        return when (finalResult){
            is Result.Success -> {
                GeneralResult.ConfirmPassword.Success()
            }
            is Result.Failure -> {
                catchException(finalResult.error)
            }
        }
    }

    override fun cancel() {
        TODO("not implemented")
    }

    private fun workOperation() : Result<String, Exception> = Result.of {
        apiClient.postUnlockDevice(password.sha256()).body
    }

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

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is ServerErrorException -> {
                when {
                    ex.errorCode == ServerCodes.BadRequest -> UIMessage(resId = R.string.password_enter_error)
                    ex.errorCode == ServerCodes.TooManyRequests -> {
                        val timeLeft = DateAndTimeUtils.getTimeInHoursAndMinutes(ex.headers?.getLong("Retry-After"))
                        if(timeLeft != null) {
                            if(timeLeft.first != 0L)
                            UIMessage(resId = R.string.too_many_requests_exception_hour,
                                    args = arrayOf(timeLeft.first))
                            else
                            UIMessage(resId = R.string.too_many_requests_exception_minute,
                                    args = arrayOf(timeLeft.second))
                        } else
                            UIMessage(resId = R.string.too_many_requests_exception_no_time_found)
                    }
                    else -> UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
                }
            }
            else -> UIMessage(resId = R.string.server_error_exception)
        }
    }
}