package com.criptext.mail.scenes.settings.custom_domain.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.CustomDomainDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.custom_domain.data.CustomDomainAPIClient
import com.criptext.mail.scenes.settings.custom_domain.data.CustomDomainResult
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError

class DeleteDomainWorker(
        private val storage: KeyValueStorage,
        private val accountDao: AccountDao,
        private val httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        private val domain: String,
        private val position: Int,
        private val domainDao: CustomDomainDao,
        override val publishFn: (
                CustomDomainResult.DeleteDomain) -> Unit)
    : BackgroundWorker<CustomDomainResult.DeleteDomain> {

    override val canBeParallelized = true
    private val apiClient = CustomDomainAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): CustomDomainResult.DeleteDomain {
        return when(ex){
            is ServerErrorException -> {
                when(ex.errorCode) {
                    ServerCodes.BadRequest ->
                        CustomDomainResult.DeleteDomain.Success(domain, position)
                    ServerCodes.TooManyDevices -> {
                        val timeLeft = DateAndTimeUtils.getTimeInHoursAndMinutes(ex.headers?.getLong("Retry-After"))
                        val message = if(timeLeft != null) {
                            if(timeLeft.first == 0L)
                                UIMessage(resId = R.string.too_many_requests_exception_minute, args = arrayOf(timeLeft.second))
                            else
                                UIMessage(resId = R.string.too_many_requests_exception_hour, args = arrayOf(timeLeft.first))
                        } else
                            UIMessage(resId = R.string.too_many_requests_exception_no_time_found)
                        CustomDomainResult.DeleteDomain.Failure(message)
                    }
                    else -> CustomDomainResult.DeleteDomain.Failure(UIMessage(R.string.server_bad_status, arrayOf(ex.errorCode)))
                }
            }
            else -> CustomDomainResult.DeleteDomain.Failure(UIMessage(R.string.unknown_error, arrayOf(ex.toString())))
        }
    }

    override fun work(reporter: ProgressReporter<CustomDomainResult.DeleteDomain>): CustomDomainResult.DeleteDomain? {
        val operation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(operation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            operation

        return when (finalResult){
            is Result.Success -> {
                CustomDomainResult.DeleteDomain.Success(domain, position)
            }
            is Result.Failure -> {
                catchException(finalResult.error)
            }
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private fun workOperation() : Result<Unit, Exception> = Result.of { apiClient.deleteDomain(domain).body }
            .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
            .flatMap { Result.of {
                domainDao.nukeTable()
            } }


    private fun newRetryWithNewSessionOperation()
            : Result<Unit, Exception> {
        val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient, activeAccount, storage, accountDao)
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
