package com.criptext.mail.scenes.emaildetail.data

import com.criptext.mail.api.CriptextAPIClient
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpResponseData
import com.criptext.mail.api.models.Event
import com.criptext.mail.api.toJSONLongArray
import com.criptext.mail.scenes.label_chooser.SelectedLabels
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by sebas on 3/12/18.
 */

class EmailDetailAPIClient(private val httpClient: HttpClient, var authToken: String): CriptextAPIClient(httpClient) {

    fun postOpenEvent(metadataKeys: List<Long>): HttpResponseData {
        val json = JSONObject()
        json.put("metadataKeys", metadataKeys.toJSONLongArray())

        return httpClient.post(path = "/event/open", authToken = authToken, body = json)
    }

    fun postUnsendEvent(metadataKey: Long, recipients: List<String>): HttpResponseData {
        val json = JSONObject()
        json.put("metadataKey", metadataKey)
        json.put("recipients", JSONArray(recipients))

        return httpClient.post(path = "/email/unsend", authToken = authToken, body = json)
    }
}
