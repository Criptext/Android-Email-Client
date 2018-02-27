package com.email.scenes.signup.data

import com.email.api.ApiCall
import okhttp3.OkHttpClient

/**
 * Created by sebas on 2/26/18.
 */

interface SignUpAPIClient  {

    fun createUser(username: String,
                   name: String,
                   password: String)
            : SignUpResult.RegisterUser

    class Default: SignUpAPIClient {
        private val client = OkHttpClient()

        override fun createUser(username: String,
                                name: String,
                                password: String)
                : SignUpResult.RegisterUser {
            val request = ApiCall.createUser(
                    username = username,
                    name = name,
                    password = password)
            val response = client.newCall(request).execute()
            return if(!response.isSuccessful){
                SignUpResult.RegisterUser.Failure(response.message())
            } else {
                SignUpResult.RegisterUser.Success(response.message())
            }
        }
    }
}
