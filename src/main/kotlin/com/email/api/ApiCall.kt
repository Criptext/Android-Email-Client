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

        fun executeRequest(client: OkHttpClient, req: Request): String {
            val response = client.newCall(req).execute()
            if (!response.isSuccessful) throw(ServerErrorException(response.code()))
            return response.body()!!.string()
        }

        fun executeFileRequest(client: OkHttpClient, req: Request): ByteArray {
            val response = client.newCall(req).execute()
            if (!response.isSuccessful) throw(ServerErrorException(response.code()))
            return response.body()!!.bytes()
        }

    }
}
