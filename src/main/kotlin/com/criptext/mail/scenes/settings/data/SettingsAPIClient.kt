package com.criptext.mail.scenes.settings.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.scenes.composer.data.PostEmailBody
import org.json.JSONArray
import org.json.JSONObject


class SettingsAPIClient(private val httpClient: HttpClient, private val token: String) {

    fun postLabelCreatedEvent(text: String, color: String): String {
        val json = JSONObject()
        val jsonPut = JSONObject()
        jsonPut.put("cmd", 308)
        json.put("text", text)
        json.put("color", color)
        jsonPut.put("params", json)

        return httpClient.post(path = "/event/peers", authToken = token, body = jsonPut)
    }


    fun postUsernameChangedEvent(newUserName: String): String {
        val json = JSONObject()
        val jsonPut = JSONObject()
        jsonPut.put("cmd", 309)
        json.put("name", newUserName)
        jsonPut.put("params", json)

        return httpClient.post(path = "/event/peers", authToken = token, body = jsonPut)
    }

    fun putUsernameChange(newUserName: String): String {
        val jsonPut = JSONObject()
        jsonPut.put("name", newUserName)

        return httpClient.put(path = "/user/name", authToken = token, body = jsonPut)
    }

    fun deleteDevice(deviceId: Int): String{
        return httpClient.delete(path = "/devices/$deviceId", authToken = token)
    }

}
