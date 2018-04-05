package com.email.scenes.composer.data

import com.email.api.ApiCall
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlin.math.log

/**
 * Created by gabriel on 3/15/18.
 */

class ComposerAPIClient(private val token: String) {

    private val client = OkHttpClient()
            .newBuilder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()


    fun findKeyBundles(recipients: List<String>, knownAddresses: Map<String, List<Int>>): String {
        val request = ApiCall.findKeyBundles(token, recipients, knownAddresses)
        return ApiCall.executeRequest(client, request)
    }

    fun postEmail(postEmailBody: PostEmailBody): String {
        val request = ApiCall.postEmail(token, postEmailBody)
        return ApiCall.executeRequest(client, request)
    }

}
