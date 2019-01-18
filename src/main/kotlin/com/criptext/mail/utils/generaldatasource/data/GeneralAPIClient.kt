package com.criptext.mail.utils.generaldatasource.data

import com.criptext.mail.api.CriptextAPIClient
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpResponseData
import com.criptext.mail.api.models.Event
import com.criptext.mail.api.toJSONLongArray
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

class GeneralAPIClient(private val httpClient: HttpClient, var token: String): CriptextAPIClient(httpClient) {

    fun postUnlockDevice(password: String): HttpResponseData {
        val json = JSONObject()
        json.put("password", password)
        return httpClient.post(path = "/device/unlock", authToken = token, body = json)
    }

    fun postLogout(): HttpResponseData{
        return httpClient.post(path = "/user/logout", authToken = token, body = JSONObject())
    }

    fun deleteAccount(password: String): HttpResponseData{
        val json = JSONObject()
        json.put("password", password)
        return httpClient.delete(path = "/user", authToken = token, body = json)
    }

    fun postForgotPassword(recipientId: String): HttpResponseData{
        val jsonPut = JSONObject()
        jsonPut.put("recipientId", recipientId)

        return httpClient.post(path = "/user/password/reset", authToken = null, body = jsonPut)
    }

    fun postLinkAccept(deviceId: String): HttpResponseData {
        val jsonPost = JSONObject()
        jsonPost.put("randomId", deviceId)
        jsonPost.put("version", UserDataWriter.FILE_SYNC_VERSION)

        return httpClient.post(path = "/link/accept", authToken = token, body = jsonPost)
    }

    fun postLinkDeny(deviceId: String): HttpResponseData {
        val jsonPost = JSONObject()
        jsonPost.put("randomId", deviceId)

        return httpClient.post(path = "/link/deny", authToken = token, body = jsonPost)
    }

    fun postSyncAccept(randomId: String): HttpResponseData {
        val jsonPost = JSONObject()
        jsonPost.put("randomId", randomId)
        jsonPost.put("version", UserDataWriter.FILE_SYNC_VERSION)

        return httpClient.post(path = "/sync/accept", authToken = token, body = jsonPost)
    }

    fun postSyncDeny(deviceId: String): HttpResponseData {
        val jsonPost = JSONObject()
        jsonPost.put("randomId", deviceId)

        return httpClient.post(path = "/sync/deny", authToken = token, body = jsonPost)
    }

    fun postFileStream(filePath: String, randomId: String): HttpResponseData {
        return httpClient.postFileStream(path = "/userdata", authToken = token, filePath = filePath,
                randomId = randomId)
    }

    fun postLinkDataReady(deviceId: Int, key: String): HttpResponseData {
        val jsonPost = JSONObject()
        jsonPost.put("deviceId", deviceId)
        jsonPost.put("key", key)
        return httpClient.post(path = "/link/data/ready", authToken = token, body = jsonPost)
    }

    fun getKeyBundle(deviceId: Int): HttpResponseData{
        return httpClient.get(path = "/keybundle/$deviceId", authToken = token)
    }

    fun putReadReceipts(readReceipts: Boolean): HttpResponseData {
        val jsonPut = JSONObject()
        jsonPut.put("enable", readReceipts)

        return httpClient.put(path = "/user/readtracking", authToken = token, body = jsonPut)
    }

    fun postSyncBegin(version: Int): HttpResponseData{
        val jsonPut = JSONObject()
        jsonPut.put("version", version)

        return httpClient.post(path = "/sync/begin", authToken = token, body = jsonPut)
    }

    fun getSyncStatus(): HttpResponseData{
        return httpClient.get(path = "/sync/status", authToken = token)
    }

    fun getFileStream(params: Map<String,String>): InputStream {
        return httpClient.getFileStream(path = "/userdata", authToken = token, params = params)
    }
}
