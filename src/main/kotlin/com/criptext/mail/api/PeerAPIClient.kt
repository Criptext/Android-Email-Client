package com.criptext.mail.api

import org.json.JSONObject

class PeerAPIClient(private val httpClient: HttpClient, var token: String): CriptextAPIClient(httpClient){
    fun postPeerEvents(jsonPost: JSONObject): String{
        return httpClient.post(path = "/event/peers", authToken = token, body = jsonPost)
    }
}