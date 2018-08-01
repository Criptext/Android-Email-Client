package com.email.api

import com.email.api.models.MultipartFormItem
import com.email.utils.file.FileUtils
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Created by gabriel on 5/8/18.
 */

interface HttpClient {
    fun post(path: String, authToken: String?, body: Map<String, MultipartFormItem>): String
    fun put(path: String, authToken: String?, body: Map<String, MultipartFormItem>): String
    fun post(path: String, authToken: String?, body: JSONObject): String
    fun put(path: String, authToken: String?, body: JSONObject): String
    fun get(path: String, authToken: String?): String
    fun getFile(path: String, authToken: String?): ByteArray

    enum class AuthScheme { basic, jwt }
    class Default(private val baseUrl: String,
                  private val authScheme: AuthScheme,
                  connectionTimeout: Long,
                  readTimeout: Long): HttpClient {

        // This is the constructor most activities should use.
        // primary constructor is more for testing.
        constructor(): this(baseUrl = Hosts.restApiBaseUrl, authScheme = AuthScheme.jwt,
                connectionTimeout = 14000L, readTimeout = 7000L)

        private val JSON = MediaType.parse("application/json; charset=utf-8")
        private val client = OkHttpClient()
                .newBuilder()
                .connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .build()

        private fun Request.Builder.addAuthorizationHeader(authToken: String?) =
            if(authToken == null) this
            else when(authScheme) {
                AuthScheme.basic -> this.addHeader("Authorization", "Basic $authToken")
                AuthScheme.jwt -> this.addHeader("Authorization", "Bearer $authToken")
            }

        private fun postJSON(url: String, authToken: String?, json: JSONObject): Request {
            val body = RequestBody.create(JSON, json.toString())
            return Request.Builder()
                    .addAuthorizationHeader(authToken)
                    .url(url)
                    .post(body)
                    .build()
        }

        private fun putJSON(url: String, authToken: String?, json: JSONObject): Request {
            val body = RequestBody.create(JSON, json.toString())
            return Request.Builder()
                    .addAuthorizationHeader(authToken)
                    .url(url)
                    .put(body)
                    .build()
        }

        private fun MultipartBody.Builder.addByteItem(name: String,
                                                      item: MultipartFormItem.ByteArrayItem)
                : MultipartBody.Builder {
            val mimeType = FileUtils.getMimeType(item.name)
            val fileBody = RequestBody.create(MediaType.parse(mimeType), item.value)
            return this.addFormDataPart(name, item.name, fileBody)
        }

        private fun MultipartBody.Builder.addFileItem(name: String,
                                                      item: MultipartFormItem.FileItem)
                : MultipartBody.Builder {
            val mimeType = FileUtils.getMimeType(item.name)
            val fileBody = RequestBody.create(MediaType.parse(mimeType), item.value)
            return this.addFormDataPart(name, item.name, fileBody)
        }

        private fun postMultipartFormData(url: String, authToken: String?,
                                 body: Map<String, MultipartFormItem>): Request {
            val multipartBody =
                body.toList().fold(MultipartBody.Builder(), { builder, (name, item) ->
                    when (item) {
                        is MultipartFormItem.StringItem ->
                            builder.addFormDataPart(name, item.value)
                        is MultipartFormItem.ByteArrayItem ->
                            builder.addByteItem(name, item)
                        is MultipartFormItem.FileItem ->
                            builder.addFileItem(name, item)
                    }
                }).build()

            return Request.Builder()
                    .addAuthorizationHeader(authToken)
                    .url(url)
                    .post(multipartBody)
                    .build()
        }

        private fun putMultipartFormData(url: String, authToken: String?,
                                          body: Map<String, MultipartFormItem>): Request {
            val multipartBody =
                    body.toList().fold(MultipartBody.Builder(), { builder, (name, item) ->
                        when (item) {
                            is MultipartFormItem.StringItem ->
                                builder.addFormDataPart(name, item.value)
                            is MultipartFormItem.ByteArrayItem ->
                                builder.addByteItem(name, item)
                            is MultipartFormItem.FileItem ->
                                builder.addFileItem(name, item)
                        }
                    }).build()

            return Request.Builder()
                    .addAuthorizationHeader(authToken)
                    .url(url)
                    .put(multipartBody)
                    .build()
        }

        private fun getUrl(url: String, authToken: String?): Request {
            return Request.Builder()
                    .addAuthorizationHeader(authToken)
                    .url(url)
                    .get()
                    .build()
        }

        override fun post(path: String, authToken: String?, body: Map<String, MultipartFormItem>): String {
            val request = postMultipartFormData(baseUrl + path, authToken, body)
            return ApiCall.executeRequest(client, request)
        }

        override fun put(path: String, authToken: String?, body: Map<String, MultipartFormItem>): String {
            val request = postMultipartFormData(baseUrl + path, authToken, body)
            return ApiCall.executeRequest(client, request)
        }

        override fun post(path: String, authToken: String?, body: JSONObject): String {
            val request = postJSON(baseUrl + path, authToken, body)
            return ApiCall.executeRequest(client, request)
        }

        override fun put(path: String, authToken: String?, body: JSONObject): String {
            val request = putJSON(baseUrl + path, authToken, body)
            return ApiCall.executeRequest(client, request)
        }

        override fun get(path: String, authToken: String?): String {
            val request = getUrl(url = baseUrl + path, authToken = authToken)
            return ApiCall.executeRequest(client, request)
        }

        override fun getFile(path: String, authToken: String?): ByteArray {
            val request = getUrl(url = baseUrl + path, authToken = authToken)
            return ApiCall.executeFileRequest(client, request)
        }
    }

}
