package com.email.scenes.mailbox.data

import com.email.api.ApiCall
import com.email.scenes.composer.data.PostEmailBody
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

    fun getBodyFromEmail(uuid: String): String {
        val request = ApiCall.getBodyFromEmail(
                token = token,
                uuid= uuid
                )
        return ApiCall.executeRequest(client, request)
    }

    fun findKeyBundles(recipients: List<String>, knownAddresses: Map<String, List<Int>>): String {
        val request = ApiCall.findKeyBundles(token, recipients, knownAddresses)
        return ApiCall.executeRequest(client, request)
    }

    fun postEmail(postEmailBody: PostEmailBody): String {
        val request = ApiCall.postEmail(token, postEmailBody)
        return ApiCall.executeRequest(client, request)
    }

}
