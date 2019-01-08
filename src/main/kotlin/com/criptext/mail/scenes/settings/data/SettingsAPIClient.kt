package com.criptext.mail.scenes.settings.data

import com.criptext.mail.api.CriptextAPIClient
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpResponseData
import com.criptext.mail.api.models.Event
import org.json.JSONObject


class SettingsAPIClient(private val httpClient: HttpClient, var token: String): CriptextAPIClient(httpClient) {

    fun putUsernameChange(newUserName: String): HttpResponseData {
        val jsonPut = JSONObject()
        jsonPut.put("name", newUserName)

        return httpClient.put(path = "/user/name", authToken = token, body = jsonPut)
    }

    fun putTwoFA(twoFA: Boolean): HttpResponseData {
        val jsonPut = JSONObject()
        jsonPut.put("enable", twoFA)

        return httpClient.put(path = "/user/2fa", authToken = token, body = jsonPut)
    }

    fun getSettings(): HttpResponseData{
        return httpClient.get(path = "/user/settings", authToken = token)
    }

    fun deleteDevice(deviceId: Int, password: String): HttpResponseData{
        val jsonObject = JSONObject()
        jsonObject.put("deviceId", deviceId)
        jsonObject.put("password", password)
        return httpClient.delete(path = "/device", authToken = token, body = jsonObject)
    }

    fun postLogout(): HttpResponseData{
        return httpClient.post(path = "/user/logout", authToken = token, body = JSONObject())
    }

    fun postForgotPassword(recipientId: String): HttpResponseData{
        val jsonPut = JSONObject()
        jsonPut.put("recipientId", recipientId)

        return httpClient.post(path = "/user/password/reset", authToken = null, body = jsonPut)
    }

}
