package com.criptext.mail.scenes.signin.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.scenes.signin.data.SignInAPIClient
import com.criptext.mail.scenes.signin.data.SignInResult
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap


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
                    ServerCodes.AuthenticationPending -> return SignInResult.LinkStatus.Waiting()
                    ServerCodes.AuthenticationDenied -> return SignInResult.LinkStatus.Denied()
                }
            }
        }
        return SignInResult.LinkStatus.Waiting()
    }

    override fun work(reporter: ProgressReporter<SignInResult.LinkStatus>): SignInResult.LinkStatus? {
        val result =  Result.of { apiClient.getLinkStatus(jwt).body }
                .flatMap { Result.of { LinkStatusData.fromJSON(it) } }

        return when (result) {
            is Result.Success ->{
                SignInResult.LinkStatus.Success(result.value)
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