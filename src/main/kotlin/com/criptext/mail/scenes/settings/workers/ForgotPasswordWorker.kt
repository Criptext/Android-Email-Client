package com.criptext.mail.scenes.settings.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.data.SettingsAPIClient
import com.criptext.mail.scenes.settings.data.SettingsResult
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError


class ForgotPasswordWorker(val httpClient: HttpClient,
                           val activeAccount: ActiveAccount,
                           private val storage: KeyValueStorage,
                           private val accountDao: AccountDao,
                           override val publishFn: (SettingsResult) -> Unit)
    : BackgroundWorker<SettingsResult.ResetPassword> {

    private var apiClient = SettingsAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = true

    override fun catchException(ex: Exception): SettingsResult.ResetPassword {
        return SettingsResult.ResetPassword.Failure(createErrorMessage(ex))
    }

    override fun work(reporter: ProgressReporter<SettingsResult.ResetPassword>): SettingsResult.ResetPassword? {
        val result = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(result)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            result

        return when (finalResult) {
            is Result.Success -> SettingsResult.ResetPassword.Success()
            is Result.Failure -> catchException(finalResult.error)
        }

    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    private fun workOperation() : Result<String, Exception> =
            Result.of {
                apiClient.postForgotPassword(activeAccount.recipientId,
                    EmailAddressUtils.extractEmailAddressDomain(activeAccount.userEmail)).body
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
            is ServerErrorException ->
                if(ex.errorCode == ServerCodes.BadRequest)
                UIMessage(resId = R.string.forgot_password_error_400)
                else
                    UIMessage(resId = R.string.forgot_password_error)
            else ->UIMessage(resId = R.string.forgot_password_error)
        }
    }

}