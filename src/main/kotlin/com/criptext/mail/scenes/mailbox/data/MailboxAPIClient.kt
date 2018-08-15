package com.criptext.mail.scenes.mailbox.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.Event
import com.criptext.mail.scenes.composer.data.PostEmailBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by sebas on 3/20/18.
 */


class MailboxAPIClient(private val httpClient: HttpClient, private val token: String) {

    fun getPendingEvents(): String {
        return httpClient.get(
                authToken = token,
                path =  "/event")
    }

    fun getEmailBody(uuid: String): String {
        return httpClient.get(
                authToken = token,
                path =  "/email/body/$uuid")
    }

    fun findKeyBundles(recipients: List<String>, knownAddresses: Map<String, List<Int>>): String {
        val jsonObject = JSONObject()
        jsonObject.put("recipients", JSONArray(recipients))
        jsonObject.put("knownAddresses", JSONObject(knownAddresses))
        return httpClient.post(authToken = token, path = "/keybundle", body = jsonObject)
    }


    fun postEmailReadChangedEvent(metadataKeys: List<Long>, unread: Boolean): String {
        val json = JSONObject()
        val jsonPost = JSONObject()
        jsonPost.put("cmd", Event.Cmd.peerEmailReadStatusUpdate)
        json.put("metadataKeys", JSONArray(metadataKeys))
        json.put("unread", if(unread) 1 else 0)
        jsonPost.put("params", json)

        return httpClient.post(path = "/event/peers", authToken = token, body = jsonPost)
    }

    fun postThreadReadChangedEvent(threadIds: List<String>, unread: Boolean): String {
        val json = JSONObject()
        val jsonPost = JSONObject()
        jsonPost.put("cmd", Event.Cmd.peerEmailThreadReadStatusUpdate)
        json.put("threadIds", JSONArray(threadIds))
        json.put("unread", if(unread) 1 else 0)
        jsonPost.put("params", json)

        return httpClient.post(path = "/event/peers", authToken = token, body = jsonPost)
    }

    fun postThreadDeletedPermanentlyEvent(threadIds: List<String>): String {
        val json = JSONObject()
        val jsonPost = JSONObject()
        jsonPost.put("cmd", Event.Cmd.peerThreadDeleted)
        json.put("threadIds", JSONArray(threadIds))
        jsonPost.put("params", json)

        return httpClient.post(path = "/event/peers", authToken = token, body = jsonPost)
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

        return httpClient.post(path = "/event/peers", authToken = token, body = jsonPost)
    }

    fun postEmail(postEmailBody: PostEmailBody): String {
        return httpClient.post(authToken = token, path = "/email", body = postEmailBody.toJSON())
    }

    fun acknowledgeEvents(eventIds: List<Long>): String {
        val jsonObject = JSONObject()
        jsonObject.put("ids", JSONArray(eventIds))

        return httpClient.post(authToken = token, path = "/event/ack", body = jsonObject)
    }

}
