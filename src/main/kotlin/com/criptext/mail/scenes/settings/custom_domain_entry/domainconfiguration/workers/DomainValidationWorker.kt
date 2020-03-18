package com.criptext.mail.scenes.settings.custom_domain_entry.domainconfiguration.workers

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
import com.criptext.mail.scenes.settings.custom_domain_entry.data.CustomDomainEntryAPIClient
import com.criptext.mail.scenes.settings.custom_domain_entry.data.CustomDomainEntryResult
import com.criptext.mail.scenes.settings.custom_domain_entry.data.DomainMXRecordsData
import com.criptext.mail.scenes.settings.custom_domain_entry.domainconfiguration.data.DomainConfigurationAPIClient
import com.criptext.mail.scenes.settings.custom_domain_entry.domainconfiguration.data.DomainConfigurationResult
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.map
import com.github.kittinunf.result.mapError

class DomainValidationWorker(
        private val storage: KeyValueStorage,
        private val accountDao: AccountDao,
        private val customDomainDao: CustomDomainDao,
        private val httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        private val domain: String,
        override val publishFn: (
                DomainConfigurationResult.ValidateDomain) -> Unit)
    : BackgroundWorker<DomainConfigurationResult.ValidateDomain> {

    override val canBeParallelized = true
    private val apiClient = DomainConfigurationAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): DomainConfigurationResult.ValidateDomain {
        return when(ex){
            is ServerErrorException -> {
                DomainConfigurationResult.ValidateDomain.Failure(ex.errorCode, UIMessage(R.string.server_bad_status, arrayOf(ex.errorCode)))
            }
            else -> DomainConfigurationResult.ValidateDomain.Failure(null, UIMessage(R.string.unknown_error, arrayOf(ex.toString())))
        }
    }

    override fun work(reporter: ProgressReporter<DomainConfigurationResult.ValidateDomain>): DomainConfigurationResult.ValidateDomain? {
        val operation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(operation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            operation

        return when (finalResult){
            is Result.Success -> {
                DomainConfigurationResult.ValidateDomain.Success()
            }
            is Result.Failure -> {
                catchException(finalResult.error)
            }
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private fun workOperation() : Result<Unit, Exception> = Result.of { apiClient.postValidateDomainMX(domain).body }
            .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
            .flatMap { Result.of {
                customDomainDao.updateValidated(domain)
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
