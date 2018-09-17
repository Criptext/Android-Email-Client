package com.criptext.mail.utils.generaldatasource.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.Event
import com.criptext.mail.api.toJSONLongArray
import org.json.JSONObject

class GeneralAPIClient(private val httpClient: HttpClient, private val token: String) {

    fun postUnlockDevice(password: String): String {
        val json = JSONObject()
        json.put("password", password)
        return httpClient.post(path = "/device/unlock", authToken = token, body = json)
    }

    fun postLogout(): String{
        return httpClient.post(path = "/user/logout", authToken = token, body = JSONObject())
    }

    fun postForgotPassword(recipientId: String): String{
        val jsonPut = JSONObject()
        jsonPut.put("recipientId", recipientId)

        return httpClient.post(path = "/user/password/reset", authToken = null, body = jsonPut)
    }

    fun postEmailDeleteEvent(metadataKeys: List<Long>): String {
        val json = JSONObject()
        val jsonPost = JSONObject()
        jsonPost.put("cmd", Event.Cmd.peerEmailDeleted)
        json.put("metadataKeys", metadataKeys.toJSONLongArray())
        jsonPost.put("params", json)

        return httpClient.post(path = "/event/peers", authToken = token, body = jsonPost)
    }

}
