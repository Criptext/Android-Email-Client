package com.criptext.mail.scenes.settings.aliases.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.AliasDao
import com.criptext.mail.db.dao.CustomDomainDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Alias
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.CustomDomain
import com.criptext.mail.scenes.settings.aliases.data.AliasesAPIClient
import com.criptext.mail.scenes.settings.aliases.data.AliasesResult
import com.criptext.mail.scenes.settings.custom_domain.data.CustomDomainAPIClient
import com.criptext.mail.scenes.settings.custom_domain.data.CustomDomainResult
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONObject

class AddAliasWorker(
        private val storage: KeyValueStorage,
        private val accountDao: AccountDao,
        private val httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        private val alias: String,
        private val domain: String?,
        private val aliasDao: AliasDao,
        override val publishFn: (
                AliasesResult.AddAlias) -> Unit)
    : BackgroundWorker<AliasesResult.AddAlias> {

    override val canBeParallelized = true
    private val apiClient = AliasesAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): AliasesResult.AddAlias {
        return when(ex){
            is ServerErrorException -> {
                when(ex.errorCode){
                    ServerCodes.BadRequest ->
                        AliasesResult.AddAlias.Failure(UIMessage(R.string.aliases_create_dialog_error))
                    ServerCodes.TooManyDevices ->
                        AliasesResult.AddAlias.Failure(UIMessage(R.string.aliases_create_dialog_max_error, arrayOf(ex.headers!!.getInt("maxAliases"))))
                    else -> AliasesResult.AddAlias.Failure(UIMessage(R.string.server_bad_status, arrayOf(ex.errorCode)))
                }
            }
            else -> AliasesResult.AddAlias.Failure(UIMessage(R.string.unknown_error, arrayOf(ex.toString())))
        }
    }

    override fun work(reporter: ProgressReporter<AliasesResult.AddAlias>): AliasesResult.AddAlias? {
        val operation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(operation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            operation

        return when (finalResult){
            is Result.Success -> {
                AliasesResult.AddAlias.Success(finalResult.value)
            }
            is Result.Failure -> {
                catchException(finalResult.error)
            }
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private fun workOperation() : Result<Alias, Exception> = Result.of {
        apiClient.postAddAlias(alias, domain ?: Contact.mainDomain).body
    }.mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
            .flatMap { Result.of {
                val jsonResponse = JSONObject(it)
                jsonResponse.getLong("addressId")
            } }
            .flatMap { Result.of { aliasDao.insert(Alias(
                    accountId = activeAccount.id,
                    id = 0,
                    name = alias,
                    rowId = it,
                    active = true,
                    domain = domain
            ))
            } }
            .flatMap {
                Result.of {
                    val dbAlias = if(domain == null){
                        aliasDao.getCriptextAliasByName(alias, activeAccount.id) ?: throw Exception()
                    } else {
                        aliasDao.getAliasByName(alias, domain, activeAccount.id) ?: throw Exception()
                    }
                    dbAlias
                }
            }


    private fun newRetryWithNewSessionOperation()
            : Result<Alias, Exception> {
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
