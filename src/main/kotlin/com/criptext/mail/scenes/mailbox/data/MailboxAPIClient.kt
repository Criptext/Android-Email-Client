package com.criptext.mail.scenes.mailbox.data

import com.criptext.mail.api.CriptextAPIClient
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpResponseData
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by sebas on 3/20/18.
 */


class MailboxAPIClient(private val httpClient: HttpClient, var token: String): CriptextAPIClient(httpClient) {

    fun getPendingEvents(): HttpResponseData {
        return httpClient.get(
                authToken = token,
                path =  "/event")
    }

    fun getPendingEvent(rowId: Int): HttpResponseData {
        return httpClient.get(
                authToken = token,
                path =  "/event/$rowId")
    }

    fun getPendingEventCount(cmd: Int): HttpResponseData {
        return httpClient.get(
                authToken = token,
                path =  "/event/$cmd/count")
    }

    fun insertPreKeys(preKeys: Map<Int, String>, excludedKeys: List<Int>): HttpResponseData {
        val jsonObject = JSONObject()
        val preKeyArray = JSONArray()
        preKeys.forEach { (id, key) ->
            if(id !in excludedKeys) {
                val item = JSONObject()
                item.put("id", id)
                item.put("publicKey", key)
                preKeyArray.put(item)
            }
        }
        jsonObject.put("preKeys", preKeyArray)

        return httpClient.put(
                authToken = token,
                body = jsonObject,
                path =  "/keybundle/prekeys")
    }

    fun acknowledgeEvents(eventIds: List<Any>): HttpResponseData {
        val jsonObject = JSONObject()
        jsonObject.put("ids", JSONArray(eventIds))

        return httpClient.post(authToken = token, path = "/event/ack", body = jsonObject)
    }

    fun getUpdateBannerData(code: Int, language: String): HttpResponseData {
        return httpClient.get(path = "/news/$language/$code", authToken = null)
    }

}
