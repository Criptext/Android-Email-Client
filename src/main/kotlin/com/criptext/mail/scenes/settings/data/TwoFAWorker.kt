package com.criptext.mail.scenes.settings.data

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.utils.ServerErrorCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result


class TwoFAWorker(val httpClient: HttpClient,
                  val activeAccount: ActiveAccount,
                  private val twoFA: Boolean,
                  override val publishFn: (SettingsResult) -> Unit)
    : BackgroundWorker<SettingsResult.Set2FA> {

    private val apiClient = SettingsAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): SettingsResult.Set2FA {
        return if(ex is ServerErrorException) {
            when(ex.errorCode) {
                ServerErrorCodes.MethodNotAllowed -> SettingsResult.Set2FA.Failure(UIMessage(R.string.message_warning_two_fa), twoFA)
                else -> SettingsResult.Set2FA.Failure(UIMessage(R.string.server_error_exception), twoFA)
            }
        }else {
            SettingsResult.Set2FA.Failure(UIMessage(R.string.server_error_exception), twoFA)
        }
    }

    override fun work(reporter: ProgressReporter<SettingsResult.Set2FA>): SettingsResult.Set2FA? {
        val result =  Result.of { apiClient.putTwoFA(twoFA) }

        return when (result) {
            is Result.Success -> SettingsResult.Set2FA.Success(twoFA)

            is Result.Failure -> catchException(result.error)
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }
}