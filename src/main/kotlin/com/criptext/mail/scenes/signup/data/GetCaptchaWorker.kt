package com.criptext.mail.scenes.signup.data

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import org.json.JSONObject

class GetCaptchaWorker(val httpClient: HttpClient,
                       override val publishFn: (SignUpResult) -> Unit)
    : BackgroundWorker<SignUpResult.GetCaptcha> {

    private val apiClient = SignUpAPIClient(httpClient)

    override val canBeParallelized = true

    override fun catchException(ex: Exception): SignUpResult.GetCaptcha {
        return SignUpResult.GetCaptcha.Failure(UIMessage(R.string.server_bad_status, arrayOf(ex)))
    }

    override fun work(reporter: ProgressReporter<SignUpResult.GetCaptcha>)
            : SignUpResult.GetCaptcha? {
        val result = Result.of {
            val response = apiClient.getCaptcha()
            val json = JSONObject(response.body)
            SVGData(json.getString("captchaKey"), json.getString("image"))
        }

        return when (result) {
            is Result.Success -> SignUpResult.GetCaptcha.Success(result.value.captchaKey, result.value.captcha)
            is Result.Failure -> {
                catchException(result.error)
            }
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    data class SVGData(val captchaKey: String, val captcha: String)
}