package com.criptext.mail.scenes.settings.custom_domain_entry.workers

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
import com.criptext.mail.scenes.composer.data.ContactDomainCheckData
import com.criptext.mail.scenes.settings.custom_domain_entry.data.CustomDomainEntryAPIClient
import com.criptext.mail.scenes.settings.custom_domain_entry.data.CustomDomainEntryResult
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError

class CheckDomainAvailableWorker(
        private val storage: KeyValueStorage,
        private val accountDao: AccountDao,
        private val customDomainDao: CustomDomainDao,
        private val httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        private val domain: String,
        override val publishFn: (
                CustomDomainEntryResult.CheckDomainAvailability) -> Unit)
    : BackgroundWorker<CustomDomainEntryResult.CheckDomainAvailability> {

    override val canBeParallelized = true
    private val apiClient = CustomDomainEntryAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): CustomDomainEntryResult.CheckDomainAvailability {
        return when(ex){
            is ServerErrorException -> {
                when(ex.errorCode) {
                    ServerCodes.BadRequest -> {
                        CustomDomainEntryResult.CheckDomainAvailability.Failure(UIMessage(R.string.custom_domain_entry_error))
                    }
                    else -> CustomDomainEntryResult.CheckDomainAvailability.Failure(UIMessage(R.string.server_bad_status, arrayOf(ex.errorCode)))
                }
            }
            is KnownExternalDomainException -> {
                CustomDomainEntryResult.CheckDomainAvailability.Failure(UIMessage(R.string.custom_domain_entry_error))
            }
            else -> CustomDomainEntryResult.CheckDomainAvailability.Failure(UIMessage(R.string.unknown_error, arrayOf(ex.toString())))
        }
    }

    override fun work(reporter: ProgressReporter<CustomDomainEntryResult.CheckDomainAvailability>): CustomDomainEntryResult.CheckDomainAvailability? {
        val operation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(operation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            operation

        return when (finalResult){
            is Result.Success -> {
                CustomDomainEntryResult.CheckDomainAvailability.Success(domain)
            }
            is Result.Failure -> {
                catchException(finalResult.error)
            }
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private fun workOperation() : Result<String, Exception> = Result.of { if(domain in ContactDomainCheckData.KNOWN_EXTERNAL_DOMAINS.map { it.name }) throw KnownExternalDomainException()}
            .flatMap { Result.of {apiClient.postDomainExist(domain).body} }
            .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)

    private fun newRetryWithNewSessionOperation()
            : Result<String, Exception> {
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

    private class KnownExternalDomainException: Exception()

}
