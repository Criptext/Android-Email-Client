package com.criptext.mail.scenes.signin.data

/**
 * Created by sebas on 2/28/18.
 */

sealed class SignInRequest{
    class AuthenticateUser(val username: String,
                           val password: String
    ): SignInRequest()

    class VerifyUser(val username: String
    ): SignInRequest()

    data class CheckUserAvailability(val username: String): SignInRequest()
}
