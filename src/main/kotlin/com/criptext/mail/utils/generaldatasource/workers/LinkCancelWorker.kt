package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result
import org.json.JSONObject


class LinkCancelWorker(val httpClient: HttpClient,
                       val username: String,
                       val domain: String,
                       val deviceId: Int?,
                       jwt: String,
                       override val publishFn: (GeneralResult) -> Unit)
    : BackgroundWorker<GeneralResult.SyncCancel> {

    private val apiClient = GeneralAPIClient(httpClient, jwt)

    override val canBeParallelized = true

    override fun catchException(ex: Exception): GeneralResult.SyncCancel {
        return GeneralResult.SyncCancel.Failure(createErrorMessage(ex))
    }

    override fun work(reporter: ProgressReporter<GeneralResult.SyncCancel>): GeneralResult.SyncCancel? {
        val result = Result.of { apiClient.postLinkCancel(username, domain, deviceId) }

        return when (result) {
            is Result.Success -> GeneralResult.SyncCancel.Success()
            is Result.Failure -> catchException(result.error)
        }

    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is ServerErrorException -> UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
            else ->UIMessage(resId = R.string.forgot_password_error)
        }
    }

}