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
        var baseUrl = "http://172.30.1.151:8000"
        private val JSON = MediaType.parse("application/json; charset=utf-8")

        fun createUser(
                username: String,
                name: String,
                password: String,
                recoveryEmail: String?
        ): Request {
            val jsonObject = JSONObject()
            jsonObject.put("username", username)
            jsonObject.put("name", name)
            jsonObject.put("password", password)
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
            return Request.Builder().url("$baseUrl/user/auth").post(body).build()
        }

        fun postKeyBundle(
                integerIdSchema : Int,
                base64StringSchema: Int
        ): Request {
            val jsonObject = JSONObject()
            jsonObject.put("integerIdSchema", integerIdSchema)
            jsonObject.put("integerIdSchema", integerIdSchema)
            val body = RequestBody.create(JSON, jsonObject.toString())
            return Request.Builder().url("$baseUrl/user/auth").post(body).build()
        }
    }
}
