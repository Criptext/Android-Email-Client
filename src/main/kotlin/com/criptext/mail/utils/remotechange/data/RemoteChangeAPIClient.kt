package com.criptext.mail.utils.remotechange.data

import com.criptext.mail.api.HttpClient
import org.json.JSONObject

class RemoteChangeAPIClient(private val httpClient: HttpClient, private val token: String) {

    fun postUnlockDevice(password: String): String {
        val json = JSONObject()
        json.put("password", password)
        return httpClient.post(path = "/device/unlock", authToken = token, body = json)
    }

    fun deleteDevice(deviceId: Int): String{
        return httpClient.delete(path = "/device/$deviceId", authToken = token)
    }

    fun postLogout(): String{
        return httpClient.post(path = "/user/logout", authToken = token, body = JSONObject())
    }

}
