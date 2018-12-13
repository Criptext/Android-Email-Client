package com.criptext.mail.push.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.Event
import com.criptext.mail.api.toJSONLongArray
import com.criptext.mail.scenes.composer.data.PostEmailBody
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
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

    fun postSyncAccept(deviceId: String): String {
        val jsonPost = JSONObject()
        jsonPost.put("randomId", deviceId)
        jsonPost.put("version", UserDataWriter.FILE_SYNC_VERSION)

        return httpClient.post(path = "/sync/accept", authToken = token, body = jsonPost)
    }

    fun postSyncDeny(randomId: String): String {
        val jsonPost = JSONObject()
        jsonPost.put("randomId", randomId)

        return httpClient.post(path = "/sync/deny", authToken = token, body = jsonPost)
    }

    fun postOpenEvent(metadataKeys: List<Long>): String {
        val json = JSONObject()
        json.put("metadataKeys", metadataKeys.toJSONLongArray())

        return httpClient.post(path = "/event/open", authToken = token, body = json)
    }

    fun postEmailLabelChangedEvent(metadataKeys: List<Long>, labelsRemoved: List<String>,
                                   labelsAdded: List<String>): String {
        val json = JSONObject()
        val jsonPost = JSONObject()
        jsonPost.put("cmd", Event.Cmd.peerEmailChangedLabels)
        json.put("metadataKeys", metadataKeys.toJSONLongArray())
        json.put("labelsRemoved", JSONArray(labelsRemoved))
        json.put("labelsAdded", JSONArray(labelsAdded))
        jsonPost.put("params", json)

        return httpClient.post(path = "/event/peers", authToken = token, body = jsonPost)
    }
}
