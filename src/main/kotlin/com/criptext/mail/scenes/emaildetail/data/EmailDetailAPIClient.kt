package com.criptext.mail.scenes.emaildetail.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.Event
import com.criptext.mail.api.toJSONLongArray
import com.criptext.mail.scenes.label_chooser.SelectedLabels
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by sebas on 3/12/18.
 */

class EmailDetailAPIClient(private val httpClient: HttpClient, private val authToken: String) {

    fun postOpenEvent(metadataKeys: List<Long>): String {
        val json = JSONObject()
        json.put("metadataKeys", metadataKeys.toJSONLongArray())

        return httpClient.post(path = "/event/open", authToken = authToken, body = json)
    }

    fun postUnsendEvent(metadataKey: Long, recipients: List<String>): String {
        val json = JSONObject()
        json.put("metadataKey", metadataKey)
        json.put("recipients", JSONArray(recipients))

        return httpClient.post(path = "/email/unsend", authToken = authToken, body = json)
    }

    fun postEmailDeleteEvent(metadataKeys: List<Long>): String {
        val json = JSONObject()
        val jsonPost = JSONObject()
        jsonPost.put("cmd", Event.Cmd.peerEmailDeleted)
        json.put("metadataKeys", metadataKeys.toJSONLongArray())
        jsonPost.put("params", json)

        return httpClient.post(path = "/event/peers", authToken = authToken, body = jsonPost)
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

        return httpClient.post(path = "/event/peers", authToken = authToken, body = jsonPost)
    }

    fun postThreadLabelChangedEvent(threadIds: List<String>, labelsRemoved: List<String>,
                                   labelsAdded: List<String>): String {
        val json = JSONObject()
        val jsonPost = JSONObject()
        jsonPost.put("cmd", Event.Cmd.peerThreadChangedLabels)
        json.put("threadIds", JSONArray(threadIds))
        json.put("labelsRemoved", JSONArray(labelsRemoved))
        json.put("labelsAdded", JSONArray(labelsAdded))
        jsonPost.put("params", json)

        return httpClient.post(path = "/event/peers", authToken = authToken, body = jsonPost)
    }

    fun postThreadDeletedPermanentlyEvent(threadIds: List<String>): String {
        val json = JSONObject()
        val jsonPost = JSONObject()
        jsonPost.put("cmd", Event.Cmd.peerThreadDeleted)
        json.put("threadIds", JSONArray(threadIds))
        jsonPost.put("params", json)

        return httpClient.post(path = "/event/peers", authToken = authToken, body = jsonPost)
    }

    fun postEmailReadChangedEvent(metadataKeys: List<Long>, unread: Boolean): String {
        val json = JSONObject()
        val jsonPost = JSONObject()
        jsonPost.put("cmd", 301)
        json.put("metadataKeys", JSONArray(metadataKeys))
        json.put("unread", if(unread) 1 else 0)
        jsonPost.put("params", json)

        return httpClient.post(path = "/event/peers", authToken = authToken, body = jsonPost)
    }

    fun postThreadReadChangedEvent(threadIds: List<String>, unread: Boolean): String {
        val json = JSONObject()
        val jsonPost = JSONObject()
        jsonPost.put("cmd", 302)
        json.put("threadIds", JSONArray(threadIds))
        json.put("unread", if(unread) 1 else 0)
        jsonPost.put("params", json)

        return httpClient.post(path = "/event/peers", authToken = authToken, body = jsonPost)
    }

}
