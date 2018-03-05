package com.email.api

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Created by sebas on 3/5/18.
 */

interface KeyServerClient {

    fun postKeyBundle(completeBundle: PreKeyBundleShareData.UploadBundle): String

    class Default : KeyServerClient {

        private val client = OkHttpClient().
                newBuilder().
                connectTimeout(5, TimeUnit.SECONDS).
                readTimeout(5, TimeUnit.SECONDS).
                build()

        override fun postKeyBundle(
                completeBundle: PreKeyBundleShareData.UploadBundle)
                : String {
            val request =  ApiCall.postKeyBundle(completeBundle)

            val response = client.newCall(request).execute()

            if(!response.isSuccessful) throw(ServerErrorException(response.code()))
            return response.message()
        }
    }
}
