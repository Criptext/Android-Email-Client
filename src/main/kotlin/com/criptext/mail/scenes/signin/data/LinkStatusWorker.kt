package com.criptext.mail.scenes.signin.data

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import org.json.JSONObject


class LinkStatusWorker(val httpClient: HttpClient,
                       private val jwt: String,
                       override val publishFn: (SignInResult) -> Unit)
    : BackgroundWorker<SignInResult.LinkStatus> {

    private val apiClient = SignInAPIClient(httpClient)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): SignInResult.LinkStatus {
        when(ex){
            is ServerErrorException -> {
                when(ex.errorCode){
                    491 -> return SignInResult.LinkStatus.Waiting()
                    493 -> return SignInResult.LinkStatus.Denied()
                }
            }
        }
        return SignInResult.LinkStatus.Waiting()
    }

    override fun work(reporter: ProgressReporter<SignInResult.LinkStatus>): SignInResult.LinkStatus? {
        val result =  Result.of { apiClient.getLinkStatus(jwt) }

        return when (result) {
            is Result.Success ->{
                val json = JSONObject(result.value)
                SignInResult.LinkStatus.Success(json.getString("name"), json.getInt("deviceId"))
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