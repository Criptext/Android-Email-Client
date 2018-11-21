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


class ReadReceiptsWorker(val httpClient: HttpClient,
                         val activeAccount: ActiveAccount,
                         private val readReceipts: Boolean,
                         override val publishFn: (SettingsResult) -> Unit)
    : BackgroundWorker<SettingsResult.SetReadReceipts> {

    private val apiClient = SettingsAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): SettingsResult.SetReadReceipts {
        return if(ex is ServerErrorException) {
            when(ex.errorCode) {
                ServerErrorCodes.MethodNotAllowed ->
                    SettingsResult.SetReadReceipts.Failure(UIMessage(R.string.message_warning_two_fa), readReceipts)
                else -> SettingsResult.SetReadReceipts.Failure(UIMessage(R.string.server_error_exception), readReceipts)
            }
        }else {
            SettingsResult.SetReadReceipts.Failure(UIMessage(R.string.server_error_exception), readReceipts)
        }
    }

    override fun work(reporter: ProgressReporter<SettingsResult.SetReadReceipts>): SettingsResult.SetReadReceipts? {
        val result =  Result.of { apiClient.putReadReceipts(readReceipts) }

        return when (result) {
            is Result.Success -> SettingsResult.SetReadReceipts.Success(readReceipts)

            is Result.Failure -> catchException(result.error)
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }
}