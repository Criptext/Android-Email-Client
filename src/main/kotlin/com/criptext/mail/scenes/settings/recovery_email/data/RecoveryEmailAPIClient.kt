package com.criptext.mail.scenes.settings.recovery_email.data

import android.accounts.NetworkErrorException
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.Event
import com.criptext.mail.utils.sha256
import org.json.JSONObject


class RecoveryEmailAPIClient(private val httpClient: HttpClient, private val token: String) {

    fun putChangeEmail(password: String, newEmail: String): String {
        val json = JSONObject()
        json.put("email", newEmail)
        json.put("password", password)
        return httpClient.post(path = "/event/peers", authToken = token, body = json)
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
