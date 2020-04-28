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
import com.criptext.mail.db.models.CustomDomain
import com.criptext.mail.scenes.settings.custom_domain.data.CustomDomainAPIClient
import com.criptext.mail.scenes.settings.custom_domain.data.CustomDomainResult
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError

class LoadDomainWorker(
        private val storage: KeyValueStorage,
        private val accountDao: AccountDao,
        private val httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        private val domainDao: CustomDomainDao,
        override val publishFn: (
                CustomDomainResult.LoadDomain) -> Unit)
    : BackgroundWorker<CustomDomainResult.LoadDomain> {

    override val canBeParallelized = false
    private val apiClient = CustomDomainAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): CustomDomainResult.LoadDomain {
        return CustomDomainResult.LoadDomain.Failure(UIMessage(R.string.unknown_error, arrayOf(ex.toString())))
    }

    override fun work(reporter: ProgressReporter<CustomDomainResult.LoadDomain>): CustomDomainResult.LoadDomain? {
        val operation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(operation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            operation

        return when (finalResult){
            is Result.Success -> {
                CustomDomainResult.LoadDomain.Success(finalResult.value)
            }
            is Result.Failure -> {
                catchException(finalResult.error)
            }
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private fun workOperation() : Result<List<CustomDomain>, Exception> = Result.of {
        val domains = domainDao.getAll(activeAccount.id)
        if(domains.isEmpty()) throw Exception()
        domains
    }


    private fun newRetryWithNewSessionOperation()
            : Result<List<CustomDomain>, Exception> {
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
