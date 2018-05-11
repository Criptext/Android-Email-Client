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
                jwt = token,
                url =  "/event")
    }

    fun getEmailBody(uuid: String): String {
        return httpClient.get(
                jwt = token,
                url =  "/email/body/$uuid")
    }

    fun findKeyBundles(recipients: List<String>, knownAddresses: Map<String, List<Int>>): String {
        val jsonObject = JSONObject()
        jsonObject.put("recipients", JSONArray(recipients))
        jsonObject.put("knownAddresses", JSONObject(knownAddresses))
        return httpClient.post(jwt = token, url = "/keybundle", body = jsonObject)
    }

    fun postEmail(postEmailBody: PostEmailBody): String {
        return httpClient.post(jwt = token, url = "/email", body = postEmailBody.toJSON())
    }

    fun acknowledgeEvents(eventIds: List<Long>): String {
        val jsonObject = JSONObject()
        jsonObject.put("ids", JSONArray(eventIds))

        return httpClient.post(jwt = token, url = "/event/ack", body = jsonObject)
    }

}
