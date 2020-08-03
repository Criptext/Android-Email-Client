package com.criptext.mail.scenes.signup.data

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import org.json.JSONObject

class CheckRecoveryEmailAvailabilityWorker(val httpClient: HttpClient,
                                           private val username: String,
                                           private val email: String,
                                           override val publishFn: (SignUpResult) -> Unit)
    : BackgroundWorker<SignUpResult.CheckRecoveryEmailAvailability> {

    private val apiClient = SignUpAPIClient(httpClient)

    override val canBeParallelized = true

    override fun catchException(ex: Exception): SignUpResult.CheckRecoveryEmailAvailability {
        return when(ex){
            is ServerErrorException -> {
                when(ex.errorCode){
                    ServerCodes.MethodNotAllowed -> {
                        val body = ex.body
                        if(body == null) {
                            SignUpResult.CheckRecoveryEmailAvailability.Failure(UIMessage(resId = R.string.recovery_email_sign_up_error))
                        } else {
                            val json = JSONObject(body)
                            when (json.getInt("error")){
                                1 -> SignUpResult.CheckRecoveryEmailAvailability.Failure(UIMessage(resId = R.string.recovery_email_sign_up_error_1))
                                2 -> SignUpResult.CheckRecoveryEmailAvailability.Failure(UIMessage(resId = R.string.recovery_email_sign_up_error_2,
                                        args = arrayOf(json.getJSONObject("data").getInt("max"))))
                                3 -> SignUpResult.CheckRecoveryEmailAvailability.Failure(UIMessage(resId = R.string.recovery_email_sign_up_error_3))
                                5 -> SignUpResult.CheckRecoveryEmailAvailability.Failure(UIMessage(R.string.recovery_email_sign_up_error_5))
                                else -> SignUpResult.CheckRecoveryEmailAvailability.Failure(UIMessage(resId = R.string.recovery_email_sign_up_error))
                            }
                        }
                    }
                    else -> SignUpResult.CheckRecoveryEmailAvailability.Failure(UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode)))
                }
            }
            else -> SignUpResult.CheckRecoveryEmailAvailability.Failure(UIMessage(resId = R.string.recovery_email_sign_up_error))
        }
    }

    override fun work(reporter: ProgressReporter<SignUpResult.CheckRecoveryEmailAvailability>)
            : SignUpResult.CheckRecoveryEmailAvailability? {
        val result = Result.of { apiClient.isRecoveryEmailAvailable(username, email) }

        return when (result) {
            is Result.Success -> SignUpResult.CheckRecoveryEmailAvailability.Success()
            is Result.Failure -> {
                catchException(result.error)
            }
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}