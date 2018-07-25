package com.email.scenes.emaildetail.data

import com.email.api.HttpClient
import com.email.api.toJSONLongArray
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

}
