package com.email.scenes.signin.data

import com.email.api.ApiCall
import com.email.api.UnprocessableEntityException
import com.email.db.models.User
import com.github.kittinunf.result.Result
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
            : Result<String, Exception>

    class Default : SignInAPIClient {
        private val client = OkHttpClient().newBuilder().connectTimeout(5, TimeUnit.SECONDS).readTimeout(5, TimeUnit.SECONDS).build()

        override fun authenticateUser(
                user: User,
                password: String,
                deviceId: Int): Result<String, Exception> {

            return Result.of {
                val request = ApiCall.authenticateUser(
                        username = user.nickname,
                        password = password,
                        deviceId = deviceId
                )
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.message()
                } else if (response.code() == 422) {
                    throw UnprocessableEntityException(response.code())
                } else {
                    TODO("thow other exception...")
                }
            }
        }
    }

}
