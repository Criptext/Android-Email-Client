package com.criptext.mail.scenes.signin.data

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.scenes.signup.data.SignUpAPIClient
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import org.json.JSONObject


class ForgotPasswordWorker(val httpClient: HttpClient,
                           private val username: String,
                           override val publishFn: (SignInResult) -> Unit)
    : BackgroundWorker<SignInResult.ForgotPassword> {

    private val apiClient = SignUpAPIClient(httpClient)

    override val canBeParallelized = true

    override fun catchException(ex: Exception): SignInResult.ForgotPassword {
        return SignInResult.ForgotPassword.Failure(createErrorMessage(ex), ex)
    }

    override fun work(reporter: ProgressReporter<SignInResult.ForgotPassword>): SignInResult.ForgotPassword? {
        val result = Result.of { apiClient.postForgotPassword(username) }

        return when (result) {
            is Result.Success ->{
                val address = JSONObject(result.value.body).getString("address")
                SignInResult.ForgotPassword.Success(address)
            }
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
                    UIMessage(resId = R.string.forgot_password_error)
            else ->UIMessage(resId = R.string.forgot_password_error)
        }
    }

}