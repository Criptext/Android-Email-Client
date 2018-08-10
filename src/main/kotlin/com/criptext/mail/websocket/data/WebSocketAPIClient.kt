package com.criptext.mail.websocket.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.scenes.composer.data.PostEmailBody
import org.json.JSONArray
import org.json.JSONObject


class WebSocketAPIClient(private val httpClient: HttpClient, private val token: String) {

    fun acknowledgeEvents(eventId: Long): String {
        val jsonObject = JSONObject()
        jsonObject.put("ids", eventId)

        return httpClient.post(authToken = token, path = "/event/ack", body = jsonObject)
    }

    fun deleteDevice(deviceId: Int): String{
        return httpClient.delete(path = "/devices/$deviceId", authToken = token)
    }

}
