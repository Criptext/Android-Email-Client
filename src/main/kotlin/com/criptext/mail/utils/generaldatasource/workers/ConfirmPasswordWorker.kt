package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.ServerErrorCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.sha256
import com.github.kittinunf.result.Result

class ConfirmPasswordWorker(private val password: String,
                            private val activeAccount: ActiveAccount,
                            private val httpClient: HttpClient,
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

        val confirmPasswordOperation = Result.of {
            apiClient.postUnlockDevice(password.sha256())
        }
        return when (confirmPasswordOperation){
            is Result.Success -> {
                GeneralResult.ConfirmPassword.Success()
            }
            is Result.Failure -> {
                catchException(confirmPasswordOperation.error)
            }
        }
    }

    override fun cancel() {
        TODO("not implemented")
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is ServerErrorException -> {
                when {
                    ex.errorCode == ServerErrorCodes.BadRequest -> UIMessage(resId = R.string.password_enter_error)
                    ex.errorCode == ServerErrorCodes.TooManyRequests -> {
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
                    else -> UIMessage(resId = R.string.server_error_exception)
                }
            }
            else -> UIMessage(resId = R.string.server_error_exception)
        }
    }
}