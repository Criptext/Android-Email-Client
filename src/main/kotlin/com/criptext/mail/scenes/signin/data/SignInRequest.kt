package com.criptext.mail.scenes.signin.data

/**
 * Created by sebas on 2/28/18.
 */

sealed class SignInRequest{
    class AuthenticateUser(val userData: UserData,
                           val isMultiple: Boolean
    ): SignInRequest()

    data class CheckUserAvailability(val username: String, val domain: String): SignInRequest()

    data class ForgotPassword(val username: String, val domain: String): SignInRequest()

    data class RecoveryCode(val recipientId: String, val domain: String, val tempToken: String, val isMultiple: Boolean, val code: String? = null): SignInRequest()

    data class LinkBegin(val username: String, val domain: String): SignInRequest()

    data class LinkAuth(val username: String, val ephemeralJwt: String, val domain: String, val password: String? = null): SignInRequest()

    data class CreateSessionFromLink(val name: String, val username: String, val domain: String, val randomId: Int,
                                     val ephemeralJwt: String, val isMultiple: Boolean): SignInRequest()
    data class LinkData(val key: String, val dataAddress: String, val authorizerId: Int): SignInRequest()

    data class LinkStatus(val ephemeralJwt: String): SignInRequest()

    class LinkDataReady: SignInRequest()

    data class FindDevices(val userData: UserData): SignInRequest()

    data class GetMaxDevices(val tempToken: String): SignInRequest()

    data class RemoveDevices(val userData: UserData, val tempToken: String,
                             val deviceIds: List<Int>, val deviceIndexes: List<Int>): SignInRequest()
}
