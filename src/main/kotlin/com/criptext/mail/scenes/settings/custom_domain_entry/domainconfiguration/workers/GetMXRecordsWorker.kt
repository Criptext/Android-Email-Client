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

class GetMXRecordsWorker(
        private val storage: KeyValueStorage,
        private val accountDao: AccountDao,
        private val httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        private val domain: String,
        override val publishFn: (
                DomainConfigurationResult.GetMXRecords) -> Unit)
    : BackgroundWorker<DomainConfigurationResult.GetMXRecords> {

    override val canBeParallelized = true
    private val apiClient = DomainConfigurationAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): DomainConfigurationResult.GetMXRecords {
        return when(ex){
            is ServerErrorException -> {
                when(ex.errorCode) {
                    ServerCodes.BadRequest ->
                        DomainConfigurationResult.GetMXRecords.NotFound(UIMessage(R.string.mx_records_not_found, arrayOf(domain)))
                    else -> DomainConfigurationResult.GetMXRecords.Failure(UIMessage(R.string.server_bad_status, arrayOf(ex.errorCode)))
                }
            }
            else -> DomainConfigurationResult.GetMXRecords.Failure(UIMessage(R.string.unknown_error, arrayOf(ex.toString())))
        }
    }

    override fun work(reporter: ProgressReporter<DomainConfigurationResult.GetMXRecords>): DomainConfigurationResult.GetMXRecords? {
        val operation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(operation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            operation

        return when (finalResult){
            is Result.Success -> {
                DomainConfigurationResult.GetMXRecords.Success(finalResult.value)
            }
            is Result.Failure -> {
                catchException(finalResult.error)
            }
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private fun workOperation() : Result<List<DomainMXRecordsData>, Exception> = Result.of { apiClient.getMXRecords(domain).body }
            .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
            .flatMap { Result.of {
                DomainMXRecordsData.fromJSON(it)
            } }


    private fun newRetryWithNewSessionOperation()
            : Result<List<DomainMXRecordsData>, Exception> {
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
