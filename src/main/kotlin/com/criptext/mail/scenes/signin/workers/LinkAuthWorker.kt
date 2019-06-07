package com.criptext.mail.scenes.signin.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.scenes.settings.devices.data.DeviceItem
import com.criptext.mail.scenes.signin.data.SignInAPIClient
import com.criptext.mail.scenes.signin.data.SignInResult
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result


class LinkAuthWorker(val httpClient: HttpClient,
                     private val jwt: String,
                     private val username: String,
                     private val domain: String,
                     private val password: String?,
                     override val publishFn: (SignInResult) -> Unit)
    : BackgroundWorker<SignInResult.LinkAuth> {

    private val apiClient = SignInAPIClient(httpClient)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): SignInResult.LinkAuth {
        return if(ex is ServerErrorException) {
            when(ex.errorCode) {
                ServerCodes.BadRequest -> SignInResult.LinkAuth.Failure(UIMessage(R.string.password_enter_error), ex)
                else -> SignInResult.LinkAuth.Failure(UIMessage(R.string.server_error_exception), ex)
            }
        }else {
            SignInResult.LinkAuth.Failure(UIMessage(R.string.server_error_exception), ex)
        }
    }

    override fun work(reporter: ProgressReporter<SignInResult.LinkAuth>): SignInResult.LinkAuth? {

        val device = DeviceItem()

        val result =  Result.of { apiClient.postLinkAuth(username, jwt, device, password, domain) }

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

    private val createErrorMessage: (ex: Exception) -> UIMessage = { _ ->
        UIMessage(resId = R.string.forgot_password_error)
    }

}