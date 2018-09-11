package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.utils.ServerErrorCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result
import org.json.JSONObject


class ForgotPasswordWorker(val httpClient: HttpClient,
                           val recipientId: String,
                           override val publishFn: (GeneralResult) -> Unit)
    : BackgroundWorker<GeneralResult.ResetPassword> {

    private val apiClient = GeneralAPIClient(httpClient, "")

    override val canBeParallelized = true

    override fun catchException(ex: Exception): GeneralResult.ResetPassword {
        return GeneralResult.ResetPassword.Failure(createErrorMessage(ex))
    }

    override fun work(reporter: ProgressReporter<GeneralResult.ResetPassword>): GeneralResult.ResetPassword? {
        val result = Result.of { apiClient.postForgotPassword(recipientId) }

        return when (result) {
            is Result.Success -> GeneralResult.ResetPassword.Success(JSONObject(result.value).getString("address"))
            is Result.Failure -> catchException(result.error)
        }

    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is ServerErrorException ->
                if(ex.errorCode == ServerErrorCodes.BadRequest)
                UIMessage(resId = R.string.forgot_password_error_400)
                else
                    UIMessage(resId = R.string.forgot_password_error)
            else ->UIMessage(resId = R.string.forgot_password_error)
        }
    }

}