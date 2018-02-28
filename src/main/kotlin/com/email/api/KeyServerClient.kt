package com.signaltest.api

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import org.whispersystems.libsignal.SignalProtocolAddress

/**
 * Created by gabriel on 11/9/17.
 */

interface KeyServerClient {
    fun postKeyBundle(completeBundle: PreKeyBundleShareData.UploadBundle)
            : Response

    fun getKeyBundle(recipientId: String, deviceId: Int): Response
    fun postMessage(senderAddress: SignalProtocolAddress, recipientAddress: SignalProtocolAddress,
                    message: String): Response
    fun getMessage(senderAddress: SignalProtocolAddress, recipientAddress: SignalProtocolAddress)
            : Response

    data class Response(val statusCode: Int, val text: String?)
    class Default: KeyServerClient {
        companion object {
            private val JSON = MediaType.parse("application/json; charset=utf-8");
            private val keyServerUrl = "http://172.30.1.20:8000"
        }

        private val client = OkHttpClient()

        private fun waitForHttpResponse(request: Request): Response {
            val response = client.newCall(request).execute()
            val responseBody = response.body()
            return Response(statusCode = response.code(), text = responseBody?.string())
        }

        private fun postJSON(url: String, json: JSONObject): Response {
            val body = RequestBody.create(JSON, json.toString())
            val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

            return waitForHttpResponse(request)
        }

        override fun postKeyBundle(completeBundle: PreKeyBundleShareData.UploadBundle): Response {
            val json = completeBundle.toJSON()

            return postJSON("$keyServerUrl/keybundle", json)
        }

        override fun getKeyBundle(recipientId: String, deviceId: Int): Response {
            val request = Request.Builder()
                        .url("$keyServerUrl/keybundle/$recipientId/$deviceId")
                        .build()
            return waitForHttpResponse(request)
        }

        override fun postMessage(senderAddress: SignalProtocolAddress,
                                 recipientAddress: SignalProtocolAddress, message: String)
                : Response {
            val sender = JSONObject()
            sender.put("recipientId", senderAddress.name)
            sender.put("deviceId", senderAddress.deviceId)

            val recipient = JSONObject()
            recipient.put("recipientId", recipientAddress.name)
            recipient.put("deviceId", recipientAddress.deviceId)

            val json = JSONObject()
            json.put("sender", sender)
            json.put("recipient", recipient)
            json.put("message", message)

            return postJSON("$keyServerUrl/message", json)
        }

        override fun getMessage(senderAddress: SignalProtocolAddress,
                                recipientAddress: SignalProtocolAddress): Response {
            val recipientIdF = recipientAddress.name
            val deviceIdF = recipientAddress.deviceId
            val recipientIdO = senderAddress.name
            val deviceIdO = senderAddress.deviceId

            val request = Request.Builder()
                    .url("$keyServerUrl/message/$recipientIdF/$deviceIdF/$recipientIdO/$deviceIdO")
                    .build()
            return waitForHttpResponse(request)
        }
    }
}