package com.criptext.mail.scenes.settings.data

import com.criptext.mail.api.CriptextAPIClient
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.Event
import org.json.JSONObject


class SettingsAPIClient(private val httpClient: HttpClient, var token: String): CriptextAPIClient(httpClient) {

    fun postLabelCreatedEvent(text: String, color: String): String {
        val json = JSONObject()
        val jsonPut = JSONObject()
        jsonPut.put("cmd", Event.Cmd.peerLabelCreated)
        json.put("text", text)
        json.put("color", color)
        jsonPut.put("params", json)

        return httpClient.post(path = "/event/peers", authToken = token, body = jsonPut)
    }


    fun postUsernameChangedEvent(newUserName: String): String {
        val json = JSONObject()
        val jsonPut = JSONObject()
        jsonPut.put("cmd", Event.Cmd.peerUserChangeName)
        json.put("name", newUserName)
        jsonPut.put("params", json)

        return httpClient.post(path = "/event/peers", authToken = token, body = jsonPut)
    }

    fun putUsernameChange(newUserName: String): String {
        val jsonPut = JSONObject()
        jsonPut.put("name", newUserName)

        return httpClient.put(path = "/user/name", authToken = token, body = jsonPut)
    }

    fun putTwoFA(twoFA: Boolean): String {
        val jsonPut = JSONObject()
        jsonPut.put("enable", twoFA)

        return httpClient.put(path = "/user/2fa", authToken = token, body = jsonPut)
    }

    fun listDevices(): String{
        return httpClient.get(path = "/devices", authToken = token)
    }

    fun getSettings(): String{
        return httpClient.get(path = "/user/settings", authToken = token)
    }

    fun deleteDevice(deviceId: Int, password: String): String{
        val jsonObject = JSONObject()
        jsonObject.put("deviceId", deviceId)
        jsonObject.put("password", password)
        return httpClient.delete(path = "/device", authToken = token, body = jsonObject)
    }

    fun postLogout(): String{
        return httpClient.post(path = "/user/logout", authToken = token, body = JSONObject())
    }

    fun postForgotPassword(recipientId: String): String{
        val jsonPut = JSONObject()
        jsonPut.put("recipientId", recipientId)

        return httpClient.post(path = "/user/password/reset", authToken = null, body = jsonPut)
    }

}
