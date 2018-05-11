package com.email.api

import java.util.concurrent.TimeUnit
import com.email.scenes.composer.data.PostEmailBody
import com.email.signal.PreKeyBundleShareData
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by sebas on 2/26/18.
 */

class ApiCall {

    companion object {
        var baseUrl = Hosts.restApiBaseUrl


        private val client = OkHttpClient()
                .newBuilder()
                .connectTimeout(7, TimeUnit.SECONDS)
                .readTimeout(7, TimeUnit.SECONDS)
                .build()

        private val JSON = MediaType.parse("application/json; charset=utf-8")

        fun executeRequest(client: OkHttpClient, req: Request): String {
            val response = client.newCall(req).execute()
            if (!response.isSuccessful) throw(ServerErrorException(response.code()))
            return response.body()!!.string()
        }

        fun executeRequest(req: Request) = executeRequest(this.client, req)

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
            if (recoveryEmail != null) jsonObject.put("recoveryEmail", recoveryEmail)
            return postJSON(url = "$baseUrl/user", json = jsonObject, jwtoken = null)
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
            return postJSON(url = "$baseUrl/user/auth", json = jsonObject, jwtoken = null)
        }

        fun findKeyBundles(token: String, recipients: List<String>, knownAddresses: Map<String, List<Int>>): Request {
            val jsonObject = JSONObject()
            jsonObject.put("recipients", JSONArray(recipients))
            jsonObject.put("knownAddresses", JSONObject(knownAddresses))
            return postJSON(jwtoken = token, url = "$baseUrl/keybundle", json = jsonObject)
        }

        fun postEmail(token: String, postEmailBody: PostEmailBody): Request {
            return postJSON(jwtoken = token, url = "$baseUrl/email",
                    json = postEmailBody.toJSON())
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
