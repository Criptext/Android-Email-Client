package com.criptext.mail.scenes.settings.data

import android.accounts.NetworkErrorException
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.Event
import com.criptext.mail.utils.sha256
import org.json.JSONObject


class SettingsAPIClient(private val httpClient: HttpClient, private val token: String) {

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

    fun listDevices(): String{
        return httpClient.get(path = "/devices", authToken = token)
    }

    fun getSettings(): String{
        return httpClient.get(path = "/user/settings", authToken = token)
    }

    fun putChangePassword(oldPassword: String, newPassword: String): String {
        val jsonObject = JSONObject()
        jsonObject.put("oldPassword", oldPassword)
        jsonObject.put("newPassword", newPassword)
        return httpClient.put(path = "/user/password/change", body = jsonObject, authToken = token)
    }

    fun deleteDevice(deviceId: Int): String{
        return httpClient.delete(path = "/device/$deviceId", authToken = token)
    }

}
