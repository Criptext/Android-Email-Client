package com.email.scenes.signin.data

/**
 * Created by sebas on 2/28/18.
 */

sealed class SignInRequest{
    class AuthenticateUser(val username: String,
                           val password: String,
                           val deviceId: Int
    ): SignInRequest()

    class VerifyUser(val username: String
    ): SignInRequest()
}
