package com.criptext.mail.utils.generaldatasource.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.Event
import com.criptext.mail.api.toJSONLongArray
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

class GeneralAPIClient(private val httpClient: HttpClient, private val token: String) {

    fun postUnlockDevice(password: String): String {
        val json = JSONObject()
        json.put("password", password)
        return httpClient.post(path = "/device/unlock", authToken = token, body = json)
    }

    fun postLogout(): String{
        return httpClient.post(path = "/user/logout", authToken = token, body = JSONObject())
    }

    fun deleteAccount(password: String): String{
        val json = JSONObject()
        json.put("password", password)
        return httpClient.delete(path = "/user", authToken = token, body = json)
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

    fun postLinkAccept(deviceId: String): String {
        val jsonPost = JSONObject()
        jsonPost.put("randomId", deviceId)

        return httpClient.post(path = "/link/accept", authToken = token, body = jsonPost)
    }

    fun postLinkDeny(deviceId: String): String {
        val jsonPost = JSONObject()
        jsonPost.put("randomId", deviceId)

        return httpClient.post(path = "/link/deny", authToken = token, body = jsonPost)
    }

    fun postFileStream(filePath: String, randomId: String): String {
        return httpClient.postFileStream(path = "/userdata", authToken = token, filePath = filePath,
                randomId = randomId)
    }

    fun postLinkDataReady(deviceId: Int, key: String): String {
        val jsonPost = JSONObject()
        jsonPost.put("deviceId", deviceId)
        jsonPost.put("key", key)
        return httpClient.post(path = "/link/data/ready", authToken = token, body = jsonPost)
    }

    fun getKeyBundle(deviceId: Int): String{
        return httpClient.get(path = "/keybundle/$deviceId", authToken = token)
    }

    fun putReadReceipts(readReceipts: Boolean): String {
        val jsonPut = JSONObject()
        jsonPut.put("enable", readReceipts)

        return httpClient.put(path = "/user/readtracking", authToken = token, body = jsonPut)
    }

    fun downloadNewsImageFile(imageUrl: Int): InputStream {
        return httpClient.getFileStream(path = "/news/security.png", authToken = token, params = emptyMap())
    }
}
