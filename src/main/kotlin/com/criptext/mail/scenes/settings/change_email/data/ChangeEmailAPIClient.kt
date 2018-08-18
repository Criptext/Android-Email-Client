package com.criptext.mail.scenes.settings.change_email.data

import android.accounts.NetworkErrorException
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.Event
import com.criptext.mail.utils.sha256
import org.json.JSONObject


class ChangeEmailAPIClient(private val httpClient: HttpClient, private val token: String) {

    fun putChangerecoveryEmail(email: String, password: String): String{
        val json = JSONObject()
        json.put("email", email)
        json.put("password", password)
        return httpClient.put(path = "/user/recovery/change", authToken = token, body = json)
    }
}
