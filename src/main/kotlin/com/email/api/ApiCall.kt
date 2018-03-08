package com.email.api

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

/**
 * Created by sebas on 2/26/18.
 */

class ApiCall {

    companion object {
        var baseUrl = "http://192.168.100.34:8000"
        private val JSON = MediaType.parse("application/json; charset=utf-8")

        fun createUser(
                name: String,
                password: String,
                recoveryEmail: String?,
                recipientId: String,
                keyBundle: PreKeyBundleShareData.UploadBundle
        ): Request {
            val jsonObject = JSONObject()
            jsonObject.put("name", name)
            jsonObject.put("password", password)
            jsonObject.put("recipientId", recipientId)
            jsonObject.put("keybundle", keyBundle.toJSON())
            if(recoveryEmail != null) jsonObject.put("recoveryEmail", recoveryEmail)
            val body = RequestBody.create(JSON, jsonObject.toString())
            return Request.
                    Builder().
                    url("$baseUrl/user").
                    post(body).
                    build()
        }

        fun authenticateUser(
                username: String,
                password: String,
                deviceId: Int
        ): Request {
            val jsonObject = JSONObject()
            jsonObject.put("username", username)
            jsonObject.put("password", password)
            jsonObject.put("deviceId", deviceId)
            val body = RequestBody.create(JSON, jsonObject.toString())
            return Request.Builder().
                    url("$baseUrl/user/auth").
                    post(body).
                    build()
        }

        private fun postJSON(url: String, json: JSONObject): Request {
            val body = RequestBody.create(JSON, json.toString())
            val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

            return request
        }
    }
}
