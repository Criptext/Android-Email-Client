package com.email.api

import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Created by gabriel on 5/8/18.
 */

interface HttpClient {
    fun post(path: String, jwt: String?, body: JSONObject): String
    fun get(path: String, jwt: String?): String

    class Default(val baseUrl: String,
                  val connectionTimeout: Long,
                  val readTimeout: Long): HttpClient {

        // This is the constructor most activities should use.
        // primary constructor is more for testing.
        constructor(): this(baseUrl = Hosts.restApiBaseUrl,
                connectionTimeout = 14000L, readTimeout = 7000L)

        private val client = OkHttpClient()
                .newBuilder()
                .connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .build()


        override fun post(path: String, jwt: String?, body: JSONObject): String {
            val request = ApiCall.postJSON(baseUrl + path, jwt, body)
            return ApiCall.executeRequest(client, request)
        }

        override fun get(path: String, jwt: String?): String {
            val request = ApiCall.getUrl(baseUrl + path, jwt)
            return ApiCall.executeRequest(client, request)
        }


    }
}
