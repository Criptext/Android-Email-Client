package com.email.scenes.emaildetail.data

import com.email.api.HttpClient
import com.email.api.toJSONLongArray
import com.email.scenes.label_chooser.SelectedLabels
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
        jsonPost.put("cmd", 305)
        json.put("metadataKeys", metadataKeys.toJSONLongArray())
        jsonPost.put("params", json)

        return httpClient.post(path = "/event/peers", authToken = authToken, body = jsonPost)
    }

    fun postEmailLabelChangedEvent(metadataKeys: List<Long>, labelsRemoved: List<String>,
                                   labelsAdded: List<String>): String {
        val json = JSONObject()
        val jsonPost = JSONObject()
        jsonPost.put("cmd", 303)
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
        jsonPost.put("cmd", 303)
        json.put("threadIds", JSONArray(threadIds))
        json.put("labelsRemoved", JSONArray(labelsRemoved))
        json.put("labelsAdded", JSONArray(labelsAdded))
        jsonPost.put("params", json)

        return httpClient.post(path = "/event/peers", authToken = authToken, body = jsonPost)
    }

    fun postThreadDeletedPermanentlyEvent(threadIds: List<String>): String {
        val json = JSONObject()
        val jsonPost = JSONObject()
        jsonPost.put("cmd", 306)
        json.put("threadIds", JSONArray(threadIds))
        jsonPost.put("params", json)

        return httpClient.post(path = "/event/peers", authToken = authToken, body = jsonPost)
    }

}
