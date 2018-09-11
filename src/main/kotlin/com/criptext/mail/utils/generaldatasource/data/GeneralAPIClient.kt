package com.criptext.mail.utils.generaldatasource.data

import com.criptext.mail.api.HttpClient
import org.json.JSONObject

class GeneralAPIClient(private val httpClient: HttpClient, private val token: String) {

    fun postUnlockDevice(password: String): String {
        val json = JSONObject()
        json.put("password", password)
        return httpClient.post(path = "/device/unlock", authToken = token, body = json)
    }

    fun postLogout(): String{
        return httpClient.post(path = "/user/logout", authToken = token, body = JSONObject())
    }

    fun postForgotPassword(recipientId: String): String{
        val jsonPut = JSONObject()
        jsonPut.put("recipientId", recipientId)

        return httpClient.post(path = "/user/password/reset", authToken = null, body = jsonPut)
    }

}
