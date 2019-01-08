package com.criptext.mail.api

open class CriptextAPIClient(val client: HttpClient) {
    fun refreshSession(refreshToken: String): HttpResponseData {
        return client.get(path = "/user/refreshtoken", authToken = refreshToken)
    }

    fun getRefreshToken(sessionToken: String): HttpResponseData {
        return client.get(path = "/user/refresh/upgrade", authToken = sessionToken)
    }
}