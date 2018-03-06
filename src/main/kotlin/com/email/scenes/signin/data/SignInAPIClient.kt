package com.email.scenes.signin.data

import com.email.api.ApiCall
import com.email.api.ServerErrorException
import com.email.db.models.User
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Created by sebas on 2/28/18.
 */

interface SignInAPIClient {

    fun authenticateUser(
            user: User,
            password: String,
            deviceId: Int)
            : String

    class Default : SignInAPIClient {
        private val client = OkHttpClient().
                newBuilder().
                connectTimeout(5, TimeUnit.SECONDS).
                readTimeout(5, TimeUnit.SECONDS).
                build()

        override fun authenticateUser(
                user: User,
                password: String,
                deviceId: Int): String {

                val request = ApiCall.authenticateUser(
                        username = user.nickname,
                        password = password,
                        deviceId = deviceId
                )
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    return response.message()
                } else if (response.code() == 422) {
                    throw ServerErrorException(response.code())
                } else {
                    TODO("thow other exception...")
                }
        }
    }

}
