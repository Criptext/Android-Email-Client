package com.criptext.mail.scenes.signin.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.scenes.signin.data.SignInAPIClient
import com.criptext.mail.scenes.signin.data.SignInResult
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import org.json.JSONObject


class LinkBeginWorker(val httpClient: HttpClient,
                      private val username: String,
                      override val publishFn: (SignInResult) -> Unit)
    : BackgroundWorker<SignInResult.LinkBegin> {

    private val apiClient = SignInAPIClient(httpClient)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): SignInResult.LinkBegin {
        when(ex){
            is ServerErrorException -> {
                when(ex.errorCode){
                    ServerCodes.BadRequest -> return SignInResult.LinkBegin.NoDevicesAvailable(createErrorMessage(ex))
                }
            }
        }
        return SignInResult.LinkBegin.Failure(createErrorMessage(ex))
    }

    override fun work(reporter: ProgressReporter<SignInResult.LinkBegin>): SignInResult.LinkBegin? {
        val result = Result.of { apiClient.postLinkBegin(username).body }
                .flatMap { Result.of {
                    val json = JSONObject(it)
                    Pair(json.getString("token"), json.getInt("twoFactorAuth") == 1)
                } }

        return when (result) {
            is Result.Success ->{
                SignInResult.LinkBegin.Success(result.value.first, result.value.second)
            }
            is Result.Failure -> catchException(result.error)
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when(ex){
            is ServerErrorException -> {
                when(ex.errorCode){
                    ServerCodes.BadRequest -> UIMessage(resId = R.string.no_devices_available)
                    ServerCodes.TooManyRequests -> UIMessage(resId = R.string.too_many_login_attempts)
                    ServerCodes.TooManyDevices -> UIMessage(resId = R.string.too_many_devices)
                    else -> UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
                }
            }
            else -> UIMessage(resId = R.string.forgot_password_error)
        }
    }

}