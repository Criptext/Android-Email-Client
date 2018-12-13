package com.criptext.mail.api

open class CriptextAPIClient(val client: HttpClient) {
    fun refreshSession(refreshToken: String): String {
        return client.get(path = "/user/refreshtoken", authToken = refreshToken)
    }

    fun getRefreshToken(sessionToken: String): String {
        return client.get(path = "/user/tokens", authToken = sessionToken)
    }
}