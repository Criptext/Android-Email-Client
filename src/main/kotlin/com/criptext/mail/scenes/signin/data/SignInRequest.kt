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

    data class ForgotPassword(val username: String): SignInRequest()

    data class LinkBegin(val username: String): SignInRequest()

    data class LinkAuth(val username: String, val ephemeralJwt: String): SignInRequest()

    data class CreateSessionFromLink(val name: String, val username: String, val randomId: Int,
                                     val ephemeralJwt: String): SignInRequest()
    data class LinkData(val key: String, val dataAddress: String, val authorizerId: Int): SignInRequest()

    data class LinkStatus(val ephemeralJwt: String): SignInRequest()

    class LinkDataReady: SignInRequest()
}
