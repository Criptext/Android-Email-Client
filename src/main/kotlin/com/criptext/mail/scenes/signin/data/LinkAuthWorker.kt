package com.criptext.mail.scenes.signin.data

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.scenes.settings.devices.DeviceItem
import com.criptext.mail.scenes.signup.data.SignUpAPIClient
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.ServerErrorCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap


class LinkAuthWorker(val httpClient: HttpClient,
                     private val jwt: String,
                     private val username: String,
                     private val password: String?,
                     override val publishFn: (SignInResult) -> Unit)
    : BackgroundWorker<SignInResult.LinkAuth> {

    private val apiClient = SignInAPIClient(httpClient)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): SignInResult.LinkAuth {
        return if(ex is ServerErrorException) {
            when(ex.errorCode) {
                ServerErrorCodes.BadRequest -> SignInResult.LinkAuth.Failure(UIMessage(R.string.password_enter_error), ex)
                else -> SignInResult.LinkAuth.Failure(UIMessage(R.string.server_error_exception), ex)
            }
        }else {
            SignInResult.LinkAuth.Failure(UIMessage(R.string.server_error_exception), ex)
        }
    }

    override fun work(reporter: ProgressReporter<SignInResult.LinkAuth>): SignInResult.LinkAuth? {

        val device = DeviceItem()

        val result =  Result.of { apiClient.postLinkAuth(username, jwt, device, password) }

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