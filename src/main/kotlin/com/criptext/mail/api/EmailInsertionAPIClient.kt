package com.criptext.mail.api

/**
 * Created by gabriel on 5/1/18.
 */

class EmailInsertionAPIClient(private val httpClient: HttpClient, private val token: String) {
    fun getBodyFromEmail(metadataKey: Long): HttpResponseData {
        return httpClient.get("/email/body/$metadataKey", token)
    }
}