package com.criptext.mail.scenes.settings.recovery_email.data

import com.criptext.mail.api.CriptextAPIClient
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpResponseData
import org.json.JSONObject


class RecoveryEmailAPIClient(private val httpClient: HttpClient, var token: String): CriptextAPIClient(httpClient) {

    fun putChangeReplyToEmail(email: String, enabled: Boolean): HttpResponseData {
        val json = JSONObject()
        if(enabled)
            json.put("address", email)
        json.put("enable", enabled)
        return httpClient.put(path = "/user/replyto", authToken = token, body = json)
    }

    fun putChangerecoveryEmail(email: String, password: String?): HttpResponseData {
        val json = JSONObject()
        json.put("email", email)
        if(password  != null) json.put("password", password)
        return httpClient.put(path = "/user/recovery/change", authToken = token, body = json)
    }

    fun putResendLink(): HttpResponseData{
        return httpClient.post(path = "/user/recovery/resend", authToken = token, body = JSONObject())
    }
}
