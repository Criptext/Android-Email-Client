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
        val baseUrl = "http://192.168.100.34:8000"

        fun createUser(username: String, name: String, password: String): Request {
            val jsonObject = JSONObject()
            jsonObject.put("username", username);
            jsonObject.put("name", name)
            jsonObject.put("password", password)
            val JSON = MediaType.parse("application/json; charset=utf-8");
            val body = RequestBody.create(JSON, jsonObject.toString());
            return Request.Builder().url("$baseUrl/user").post(body).build()
        }
    }
}
