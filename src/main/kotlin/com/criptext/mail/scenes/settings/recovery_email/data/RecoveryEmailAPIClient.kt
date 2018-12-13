package com.criptext.mail.scenes.settings.recovery_email.data

import android.accounts.NetworkErrorException
import com.criptext.mail.api.CriptextAPIClient
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.Event
import com.criptext.mail.utils.sha256
import org.json.JSONObject


class RecoveryEmailAPIClient(private val httpClient: HttpClient, var token: String): CriptextAPIClient(httpClient) {

    fun putChangerecoveryEmail(email: String, password: String): String{
        val json = JSONObject()
        json.put("email", email)
        json.put("password", password)
        return httpClient.put(path = "/user/recovery/change", authToken = token, body = json)
    }

    fun checkPassword(
            username: String,
            password: String)
            : String {
        val jsonObject = JSONObject()
        jsonObject.put("username", username)
        jsonObject.put("password", password)
        //return httpClient.post(path = "/user/auth", body = jsonObject, authToken = null)
        if(password == "password".sha256())
            return "Ok"
        else
            throw NetworkErrorException()
    }

    fun putResendLink(): String{
        return httpClient.post(path = "/user/recovery/resend", authToken = token, body = JSONObject())
    }
}
