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


class RemoveDevicesWorker(val httpClient: HttpClient,
                          private val userData: UserData,
                          private val tempToken: String,
                          private val deviceIds: List<Int>,
                          private val deviceIndexes: List<Int>,
                          override val publishFn: (SignInResult) -> Unit)
    : BackgroundWorker<SignInResult.RemoveDevices> {

    private val apiClient = SignUpAPIClient(httpClient)

    override val canBeParallelized = true

    override fun catchException(ex: Exception): SignInResult.RemoveDevices {
        return SignInResult.RemoveDevices.Failure(createErrorMessage(ex), ex)
    }

    override fun work(reporter: ProgressReporter<SignInResult.RemoveDevices>): SignInResult.RemoveDevices? {
        val result = Result.of {
            apiClient.deleteDevices(
                devicesIds = deviceIds,
                domain = userData.domain,
                recipientId = userData.username,
                token = tempToken)
        }

        return when (result) {
            is Result.Success ->{
                SignInResult.RemoveDevices.Success(deviceIds)
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