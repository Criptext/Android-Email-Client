package com.criptext.mail.api

import com.criptext.mail.utils.ContactUtils
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

    fun postReportSpam(emails: List<String>, type: ContactUtils.ContactReportTypes, data: String?): HttpResponseData{
        val json = JSONObject()
        json.put("emails", emails.toJSONStringArray())
        json.put("type", type.name)
        json.put("headers", data)
        return httpClient.post(path = "/contact/report", authToken = token, body = json)
    }
}