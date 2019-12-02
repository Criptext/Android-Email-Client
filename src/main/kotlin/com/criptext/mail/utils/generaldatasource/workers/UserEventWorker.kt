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


class UserEventWorker(val httpClient: HttpClient,
                      val event: Int,
                      activeAccount: ActiveAccount,
                      override val publishFn: (GeneralResult) -> Unit)
    : BackgroundWorker<GeneralResult.UserEvent> {

    private val apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = true

    override fun catchException(ex: Exception): GeneralResult.UserEvent {
        return GeneralResult.UserEvent.Failure(createErrorMessage(ex))
    }

    override fun work(reporter: ProgressReporter<GeneralResult.UserEvent>): GeneralResult.UserEvent? {
        val result = Result.of { apiClient.postUserEvent(event) }

        return when (result) {
            is Result.Success -> GeneralResult.UserEvent.Success()
            is Result.Failure -> catchException(result.error)
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is ServerErrorException ->
                UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
            else -> UIMessage(resId = R.string.unknown_error, args = arrayOf(ex.toString()))
        }
    }

}