package com.criptext.mail.scenes.settings.changepassword.data

import com.criptext.mail.api.HttpClient
import org.json.JSONObject


class ChangePasswordAPIClient(private val httpClient: HttpClient, private val token: String) {
    fun putChangePassword(oldPassword: String, newPassword: String): String {
        val jsonObject = JSONObject()
        jsonObject.put("oldPassword", oldPassword)
        jsonObject.put("newPassword", newPassword)
        return httpClient.put(path = "/user/password/change", body = jsonObject, authToken = token)
    }
}
