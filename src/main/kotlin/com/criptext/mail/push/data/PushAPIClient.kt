package com.criptext.mail.push.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpResponseData
import com.criptext.mail.api.models.Event
import com.criptext.mail.api.toJSONLongArray
import com.criptext.mail.scenes.composer.data.PostEmailBody
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import org.json.JSONArray
import org.json.JSONObject


class PushAPIClient(private val httpClient: HttpClient, private val token: String) {

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

    fun postSyncAccept(deviceId: String): HttpResponseData {
        val jsonPost = JSONObject()
        jsonPost.put("randomId", deviceId)
        jsonPost.put("version", UserDataWriter.FILE_SYNC_VERSION)

        return httpClient.post(path = "/sync/accept", authToken = token, body = jsonPost)
    }

    fun postSyncDeny(randomId: String): HttpResponseData {
        val jsonPost = JSONObject()
        jsonPost.put("randomId", randomId)

        return httpClient.post(path = "/sync/deny", authToken = token, body = jsonPost)
    }
}
