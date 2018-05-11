package com.email.api

/**
 * Created by gabriel on 5/1/18.
 */

class EmailInsertionAPIClient(private val httpClient: HttpClient, private val token: String) {
    fun getBodyFromEmail(messageId: String): String {
        return httpClient.get("/email/$messageId", token)
    }
}