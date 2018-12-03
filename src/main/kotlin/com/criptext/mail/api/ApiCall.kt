package com.criptext.mail.api

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.InputStream

/**
 * Created by sebas on 2/26/18.
 */

class ApiCall {

    companion object {

        fun executeRequest(client: OkHttpClient, req: Request): String {
            val response = client.newCall(req).execute()
            if (!response.isSuccessful) {
                val rateLimitHeader = response.header("Retry-After")?.toLong()
                val resultHeaders = ResultHeaders(response.headers())
                throw(ServerErrorException(response.code(), rateLimitHeader, resultHeaders))
            }
            return response.body()!!.string()
        }

        fun executeFileRequest(client: OkHttpClient, req: Request): ByteArray {
            val response = client.newCall(req).execute()
            if (!response.isSuccessful) throw(ServerErrorException(response.code()))
            return response.body()!!.bytes()
        }

        fun executeInputStreamRequest(client: OkHttpClient, req: Request): InputStream {
            val response = client.newCall(req).execute()
            if (!response.isSuccessful) throw(ServerErrorException(response.code()))
            return response.body()!!.byteStream()
        }

    }
}
