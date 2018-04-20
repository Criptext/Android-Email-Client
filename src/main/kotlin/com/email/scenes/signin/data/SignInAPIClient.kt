package com.email.scenes.signin.data

import com.email.api.ApiCall
import com.email.api.ServerErrorException
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Created by sebas on 2/28/18.
 */

interface SignInAPIClient {

    fun authenticateUser(
            username: String,
            password: String,
            deviceId: Int)
            : String

    class Default : SignInAPIClient {
        private val client = OkHttpClient().
                newBuilder().
                connectTimeout(60, TimeUnit.SECONDS).
                readTimeout(60, TimeUnit.SECONDS).
                build()

        override fun authenticateUser(
                username: String,
                password: String,
                deviceId: Int): String {

                val request = ApiCall.authenticateUser(
                        username = username,
                        password = password,
                        deviceId = deviceId
                )
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    return response.message()
                } else
                    throw ServerErrorException(response.code())
        }
    }

}
