package com.criptext.mail.scenes.signin.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.api.models.Event
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.signin.data.SignInAPIClient
import com.criptext.mail.scenes.signin.data.SignInResult
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import org.json.JSONObject


class LinkDataReadyWorker(private val activeAccount: ActiveAccount,
                          val httpClient: HttpClient,
                          override val publishFn: (SignInResult) -> Unit)
    : BackgroundWorker<SignInResult.LinkDataReady> {

    private val apiClient = SignInAPIClient(httpClient)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): SignInResult.LinkDataReady {
        return SignInResult.LinkDataReady.Failure(createErrorMessage(ex), ex)
    }

    override fun work(reporter: ProgressReporter<SignInResult.LinkDataReady>): SignInResult.LinkDataReady? {
        val result = Result.of { apiClient.isLinkDataReady(activeAccount.jwt) }
                .flatMap { Result.of { Event.fromJSON(it.body) } }
                .flatMap { Result.of {
                    Pair(Pair(
                            JSONObject(it.params).getString("key"),
                            JSONObject(it.params).getString("dataAddress")
                    ), it.rowid)
                } }
                .flatMap { Result.of { apiClient.acknowledgeEvents(listOf(it.second), activeAccount.jwt)
                it.first} }

        return when (result) {
            is Result.Success ->{

                SignInResult.LinkDataReady.Success(key = result.value.first,
                        dataAddress = result.value.second)
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