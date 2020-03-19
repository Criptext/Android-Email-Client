package com.criptext.mail.scenes.settings.aliases.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.AliasDao
import com.criptext.mail.db.dao.CustomDomainDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Alias
import com.criptext.mail.scenes.settings.aliases.data.AliasesAPIClient
import com.criptext.mail.scenes.settings.aliases.data.AliasesResult
import com.criptext.mail.scenes.settings.custom_domain.data.CustomDomainAPIClient
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError

class EnableAliasWorker(
        private val storage: KeyValueStorage,
        private val accountDao: AccountDao,
        private val httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        private val alias: String,
        private val domain: String?,
        private val position: Int,
        private val enable: Boolean,
        private val aliasDao: AliasDao,
        override val publishFn: (
                AliasesResult.EnableAlias) -> Unit)
    : BackgroundWorker<AliasesResult.EnableAlias> {

    override val canBeParallelized = true
    private val apiClient = AliasesAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): AliasesResult.EnableAlias {
        return AliasesResult.EnableAlias.Failure(alias, domain, position, enable, UIMessage(R.string.unknown_error, arrayOf(ex.toString())))
    }

    override fun work(reporter: ProgressReporter<AliasesResult.EnableAlias>): AliasesResult.EnableAlias? {
        val operation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(operation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            operation

        return when (finalResult){
            is Result.Success -> {
                AliasesResult.EnableAlias.Success(alias, domain, position, enable)
            }
            is Result.Failure -> {
                catchException(finalResult.error)
            }
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private fun workOperation() : Result<Unit, Exception> = Result.of {
        val alias = (if(domain == null) aliasDao.getCriptextAliasByName(alias, activeAccount.id)
        else aliasDao.getAliasByName(alias, domain, activeAccount.id))
                ?: throw Exception()
        apiClient.putAliasActive(alias.rowId, enable)
    }.mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
    .flatMap { Result.of {
        if(domain != null) {
            aliasDao.updateActive(alias, domain, enable, activeAccount.id)
        } else {
            aliasDao.updateCriptextActive(alias, enable, activeAccount.id)
        }
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
