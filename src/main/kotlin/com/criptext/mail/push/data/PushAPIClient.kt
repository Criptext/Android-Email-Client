package com.criptext.mail.push.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.Event
import com.criptext.mail.api.toJSONLongArray
import com.criptext.mail.scenes.composer.data.PostEmailBody
import org.json.JSONArray
import org.json.JSONObject


class PushAPIClient(private val httpClient: HttpClient, private val token: String) {

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
}
