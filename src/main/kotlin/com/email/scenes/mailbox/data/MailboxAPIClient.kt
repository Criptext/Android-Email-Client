package com.email.scenes.mailbox.data

import com.email.api.ApiCall
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Created by sebas on 3/20/18.
 */


class MailboxAPIClient(private val token: String) {

    private val client = OkHttpClient().
            newBuilder().
            connectTimeout(20, TimeUnit.SECONDS).
            readTimeout(20, TimeUnit.SECONDS).
            build()


    fun getPendingEvents(): String {
        val request = ApiCall.getPendingEvents(token = token)
        return ApiCall.executeRequest(client, request)
    }
}
