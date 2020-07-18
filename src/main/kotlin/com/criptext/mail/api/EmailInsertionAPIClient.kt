package com.criptext.mail.api

import org.json.JSONObject

/**
 * Created by gabriel on 5/1/18.
 */

class EmailInsertionAPIClient(private val httpClient: HttpClient, private val token: String) {
    fun getBodyFromEmail(metadataKey: Long): HttpResponseData {
        return httpClient.get("/email/body/$metadataKey", token)
    }

    fun postEmailReEncrypt(metadataKey: Long, eventId: Any): HttpResponseData {
        val json = JSONObject()
        json.put("metadataKey", metadataKey)
        json.put("eventid", eventId)
        return httpClient.post("/email/reencrypt", token, json)
    }
}