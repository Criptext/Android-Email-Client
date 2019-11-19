package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.utils.ContactUtils
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result


class ReportWorker(val httpClient: HttpClient,
                   val email: List<String>,
                   val type: ContactUtils.ContactReportTypes,
                   activeAccount: ActiveAccount,
                   override val publishFn: (GeneralResult) -> Unit)
    : BackgroundWorker<GeneralResult.Report> {

    private val apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = true

    override fun catchException(ex: Exception): GeneralResult.Report {
        return GeneralResult.Report.Failure(createErrorMessage(ex))
    }

    override fun work(reporter: ProgressReporter<GeneralResult.Report>): GeneralResult.Report? {
        val result = Result.of { apiClient.postReportSpam(email, type, null) }

        return when (result) {
            is Result.Success -> GeneralResult.Report.Success()
            is Result.Failure -> catchException(result.error)
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is ServerErrorException ->
                if(ex.errorCode == ServerCodes.BadRequest)
                UIMessage(resId = R.string.forgot_password_error_400)
                else
                    UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
            else ->UIMessage(resId = R.string.forgot_password_error)
        }
    }

}