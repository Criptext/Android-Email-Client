package com.criptext.mail.api

import org.json.JSONObject

class PeerAPIClient(private val httpClient: HttpClient, private val token: String){
    fun postPeerEvents(jsonPost: JSONObject): String{
        return httpClient.post(path = "/event/peers", authToken = token, body = jsonPost)
    }
}