package com.email.api

/**
 * Created by gabriel on 5/1/18.
 */

class EmailInsertionAPIClient(private val token: String) {
    fun getBodyFromEmail(uuid: String): String {
        val request = ApiCall.getBodyFromEmail(
                token = token,
                uuid= uuid
                )
        return ApiCall.executeRequest(request)
    }
}