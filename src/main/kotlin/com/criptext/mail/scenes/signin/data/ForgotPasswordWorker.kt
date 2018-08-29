package com.criptext.mail.scenes.signin.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.scenes.signup.data.SignUpAPIClient
import com.criptext.mail.scenes.signup.data.SignUpResult
import com.github.kittinunf.result.Result


class ForgotPasswordWorker(val httpClient: HttpClient,
                           private val username: String,
                           override val publishFn: (SignInResult) -> Unit)
    : BackgroundWorker<SignInResult.ForgotPassword> {

    private val apiClient = SignUpAPIClient(httpClient)

    override val canBeParallelized = true

    override fun catchException(ex: Exception): SignInResult.ForgotPassword {
        return SignInResult.ForgotPassword.Failure()
    }

    override fun work(reporter: ProgressReporter<SignInResult.ForgotPassword>): SignInResult.ForgotPassword? {
        val result = Result.of { apiClient.postForgotPassword(username) }

        return when (result) {
            is Result.Success -> SignInResult.ForgotPassword.Success()
            is Result.Failure -> SignInResult.ForgotPassword.Failure()
            }

    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}