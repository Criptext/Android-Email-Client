package com.criptext.mail.api

import com.criptext.mail.BuildConfig
import com.criptext.mail.api.models.MultipartFormItem
import com.criptext.mail.utils.LoggingInterceptor
import com.criptext.mail.utils.file.FileUtils
import okhttp3.*
import org.json.JSONObject
import java.io.FileInputStream
import java.io.InputStream
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
    fun delete(path: String, authToken: String?, body: JSONObject): String
    fun getFile(path: String, authToken: String?): ByteArray
    fun postFileStream(path: String, authToken: String?, filePath: String, randomId: String): String
    fun getFileStream(path: String, authToken: String?, params: Map<String, String>): InputStream

    enum class AuthScheme { basic, jwt }
    class Default(private val baseUrl: String,
                  private val authScheme: AuthScheme,
                  private val connectionTimeout: Long,
                  private val readTimeout: Long): HttpClient {

        // This is the constructor most activities should use.
        // primary constructor is more for testing.
        constructor() : this(baseUrl = Hosts.restApiBaseUrl, authScheme = AuthScheme.jwt,
                connectionTimeout = 14000L, readTimeout = 7000L)

        private val JSON = MediaType.parse("application/json; charset=utf-8")
        private val MEDIA_TYPE_PLAINTEXT = MediaType.parse("text/plain; charset=utf-8")

        private val client = buildClient()

        private fun buildClient(): OkHttpClient {
            val okClient = OkHttpClient()
                    .newBuilder()
                    .connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
                    .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
            if(BuildConfig.DEBUG){
                okClient.addInterceptor(LoggingInterceptor())
            }
            return okClient.build()
        }

        private fun Request.Builder.addAuthorizationHeader(authToken: String?) =
                if (authToken == null) this
                else when (authScheme) {
                    AuthScheme.basic -> this.addHeader("Authorization", "Basic $authToken")
                    AuthScheme.jwt -> this.addHeader("Authorization", "Bearer $authToken")
                }

        private fun Request.Builder.addApiVersionHeader() =
                this.addHeader("API-Version", "$API_VERSION")

        private fun deleteJSON(url: String, authToken: String?, json: JSONObject): Request {
            val newUrl = HttpUrl.parse(url)!!.newBuilder()
            val url = newUrl.build()
            val body = RequestBody.create(JSON, json.toString())
            return Request.Builder()
                    .addAuthorizationHeader(authToken)
                    .addApiVersionHeader()
                    .url(url)
                    .delete(body)
                    .build()
        }

        private fun postJSON(url: String, authToken: String?, json: JSONObject): Request {
            val body = RequestBody.create(JSON, json.toString())
            return Request.Builder()
                    .addAuthorizationHeader(authToken)
                    .addApiVersionHeader()
                    .url(url)
                    .post(body)
                    .build()
        }

        private fun postStream(url: String, authToken: String?, filePath: String,
                               randomId: String): Request {
            val body = StreamRequest(MEDIA_TYPE_PLAINTEXT, filePath)
            return Request.Builder()
                    .addAuthorizationHeader(authToken)
                    .addHeader("random-id", randomId)
                    .url(url)
                    .post(body)
                    .build()
        }

        private fun putJSON(url: String, authToken: String?, json: JSONObject): Request {
            val body = RequestBody.create(JSON, json.toString())
            return Request.Builder()
                    .addAuthorizationHeader(authToken)
                    .addApiVersionHeader()
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
                    .addApiVersionHeader()
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
                    .addApiVersionHeader()
                    .url(url)
                    .put(multipartBody)
                    .build()
        }

        private fun getUrl(url: String, authToken: String?): Request {
            return Request.Builder()
                    .addAuthorizationHeader(authToken)
                    .addApiVersionHeader()
                    .url(url)
                    .get()
                    .build()
        }

        private fun getUrlWithQueryParams(url: String, authToken: String?, params: Map<String, String>): Request {
            val newUrl = HttpUrl.parse(url)!!.newBuilder()
            for (key in params.keys) {
                newUrl.addQueryParameter(key, params[key])
            }
            return Request.Builder()
                    .addAuthorizationHeader(authToken)
                    .url(newUrl.build())
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

        override fun delete(path: String, authToken: String?, body: JSONObject): String {
            val request = deleteJSON(url = baseUrl + path, authToken = authToken, json = body)
            return ApiCall.executeRequest(client, request)
        }

        override fun getFile(path: String, authToken: String?): ByteArray {
            val request = getUrl(url = baseUrl + path, authToken = authToken)
            return ApiCall.executeFileRequest(client, request)
        }

        override fun postFileStream(path: String, authToken: String?, filePath: String, randomId: String): String {
            val request = postStream(url = baseUrl + path, authToken = authToken,
                    filePath = filePath, randomId = randomId)
            return ApiCall.executeRequest(client, request)
        }

        override fun getFileStream(path: String, authToken: String?, params: Map<String, String>): InputStream {
            val request = getUrlWithQueryParams(url = baseUrl + path, authToken = authToken, params = params)
            return ApiCall.executeInputStreamRequest(client, request)
        }

        companion object {
            const val API_VERSION = 1.0
        }
    }
}
