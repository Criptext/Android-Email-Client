package com.criptext.mail.scenes.signin.data

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.scenes.signup.data.SignUpAPIClient
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap


class LinkAuthWorker(val httpClient: HttpClient,
                     private val jwt: String,
                     private val username: String,
                     override val publishFn: (SignInResult) -> Unit)
    : BackgroundWorker<SignInResult.LinkAuth> {

    private val apiClient = SignUpAPIClient(httpClient)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): SignInResult.LinkAuth {
        return SignInResult.LinkAuth.Failure(createErrorMessage(ex), ex)
    }

    override fun work(reporter: ProgressReporter<SignInResult.LinkAuth>): SignInResult.LinkAuth? {
        val result =  Result.of { apiClient.postLinkAuth(username, jwt) }

        return when (result) {
            is Result.Success ->{
                SignInResult.LinkAuth.Success()
            }
            is Result.Failure -> catchException(result.error)
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        UIMessage(resId = R.string.forgot_password_error)
    }

}