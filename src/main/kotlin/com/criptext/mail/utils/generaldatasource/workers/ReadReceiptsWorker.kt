package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.data.SettingsAPIClient
import com.criptext.mail.scenes.settings.data.SettingsResult
import com.criptext.mail.utils.ServerErrorCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result


class ReadReceiptsWorker(val httpClient: HttpClient,
                         val activeAccount: ActiveAccount,
                         private val readReceipts: Boolean,
                         override val publishFn: (GeneralResult) -> Unit)
    : BackgroundWorker<GeneralResult.SetReadReceipts> {

    private val apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): GeneralResult.SetReadReceipts {
        return if(ex is ServerErrorException) {
            when(ex.errorCode) {
                ServerErrorCodes.MethodNotAllowed ->
                    GeneralResult.SetReadReceipts.Failure(UIMessage(R.string.message_warning_two_fa), readReceipts)
                else -> GeneralResult.SetReadReceipts.Failure(UIMessage(R.string.server_error_exception), readReceipts)
            }
        }else {
            GeneralResult.SetReadReceipts.Failure(UIMessage(R.string.server_error_exception), readReceipts)
        }
    }

    override fun work(reporter: ProgressReporter<GeneralResult.SetReadReceipts>): GeneralResult.SetReadReceipts? {
        val result =  Result.of { apiClient.putReadReceipts(readReceipts) }

        return when (result) {
            is Result.Success -> GeneralResult.SetReadReceipts.Success(readReceipts)

            is Result.Failure -> catchException(result.error)
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }
}