package com.email.api

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

/**
 * Created by sebas on 2/26/18.
 */

class ApiCall {

    companion object {
        var baseUrl = Hosts.restApiBaseUrl

        private val JSON = MediaType.parse("application/json; charset=utf-8")

        fun executeRequest(client: OkHttpClient, req: Request): String {
            val response = client.newCall(req).execute()
            if (!response.isSuccessful) throw(ServerErrorException(response.code()))
            return response.body()!!.string()
        }

        fun postJSON(url: String, jwtoken: String?, json: JSONObject): Request {
            val body = RequestBody.create(JSON, json.toString())
            var builder = Request.Builder()
                    .url(url)
                    .post(body)

            if (jwtoken != null) {
                builder = builder.addHeader("Authorization", "Bearer $jwtoken")
            }

            return builder.build()
        }

        fun getUrl(url: String, jwtoken: String?): Request {
            var builder = Request.Builder()
                    .url(url)
                    .get()

            if (jwtoken != null) {
                builder = builder.addHeader("Authorization", "Bearer $jwtoken")
            }

            return builder.build()
        }
    }
}
