package com.email.scenes.mailbox.data

import com.email.api.HttpClient
import com.email.scenes.composer.data.PostEmailBody
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

    fun postEmail(postEmailBody: PostEmailBody): String {
        return httpClient.post(authToken = token, path = "/email", body = postEmailBody.toJSON())
    }

    fun acknowledgeEvents(eventIds: List<Long>): String {
        val jsonObject = JSONObject()
        jsonObject.put("ids", JSONArray(eventIds))

        return httpClient.post(authToken = token, path = "/event/ack", body = jsonObject)
    }

}
