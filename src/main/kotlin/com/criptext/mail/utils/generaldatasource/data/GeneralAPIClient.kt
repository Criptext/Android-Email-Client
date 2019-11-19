package com.criptext.mail.utils.generaldatasource.data

import com.criptext.mail.api.*
import com.criptext.mail.api.models.Event
import com.criptext.mail.utils.ContactUtils
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

    fun postForgotPassword(recipientId: String, domain: String): HttpResponseData{
        val jsonPut = JSONObject()
        jsonPut.put("recipientId", recipientId)
        jsonPut.put("domain", domain)

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

    fun postLinkCancel(recipientId: String, domain: String, deviceId: Int?): HttpResponseData{
        val json = JSONObject()
        json.put("recipientId", recipientId)
        json.put("domain", domain)
        json.put("deviceId", deviceId)
        return httpClient.post(path = "/link/cancel", authToken = token, body = json)
    }

    fun postSyncCancel(): HttpResponseData{
        return httpClient.post(path = "/sync/cancel", authToken = token, body = JSONObject())
    }

    fun postReportSpam(emails: List<String>, type: ContactUtils.ContactReportTypes, data: String?): HttpResponseData{
        val json = JSONObject()
        json.put("emails", emails.toJSONStringArray())
        json.put("type", type.name)
        json.put("headers", data)
        return httpClient.post(path = "/contact/report", authToken = token, body = json)
    }
}
