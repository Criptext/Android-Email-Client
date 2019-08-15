package com.criptext.mail.scenes.signin.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.scenes.settings.devices.data.DeviceItem
import com.criptext.mail.scenes.signin.data.SignInResult
import com.criptext.mail.scenes.signin.data.UserData
import com.criptext.mail.scenes.signup.data.SignUpAPIClient
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import org.json.JSONObject


class GetDevicesWorker(val httpClient: HttpClient,
                       private val userData: UserData,
                       override val publishFn: (SignInResult) -> Unit)
    : BackgroundWorker<SignInResult.FindDevices> {

    private val apiClient = SignUpAPIClient(httpClient)

    override val canBeParallelized = true

    override fun catchException(ex: Exception): SignInResult.FindDevices {
        return SignInResult.FindDevices.Failure(createErrorMessage(ex), ex)
    }

    override fun work(reporter: ProgressReporter<SignInResult.FindDevices>): SignInResult.FindDevices? {
        val result = Result.of { apiClient.postFindDevices(userData.username, userData.domain, userData.password) }
            .flatMap { Result.of {
                val jsonResponse = JSONObject(it.body)
                Pair(jsonResponse.getString("token"), DeviceItem.fromJSON(jsonResponse.getJSONArray("devices").toString()))
            }
        }

        return when (result) {
            is Result.Success ->{
                SignInResult.FindDevices.Success(result.value.first, ArrayList(result.value.second))
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
                UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
            else ->UIMessage(resId = R.string.server_error_exception)
        }
    }

}