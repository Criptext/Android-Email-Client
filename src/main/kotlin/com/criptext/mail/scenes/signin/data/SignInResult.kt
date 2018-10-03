package com.criptext.mail.scenes.signin.data

import com.criptext.mail.utils.UIMessage

/**
 * Created by sebas on 2/28/18.
 */

sealed class SignInResult {

    sealed class AuthenticateUser: SignInResult() {
        class Success: AuthenticateUser()
        data class Failure(
                val message: UIMessage,
                val exception: Exception): AuthenticateUser()
    }

    sealed class VerifyUser: SignInResult() {
        class Success: VerifyUser()
        data class Failure(
                val message: UIMessage,
                val exception: Exception): VerifyUser()
    }

    sealed class CheckUsernameAvailability: SignInResult() {
        data class Success(val userExists: Boolean, val username: String): CheckUsernameAvailability()
        class Failure: CheckUsernameAvailability()
    }

    sealed class ForgotPassword: SignInResult() {
        data class Success(val emailAddress: String): ForgotPassword()
        data class Failure(val message: UIMessage,
                val exception: Exception): ForgotPassword()
    }

    sealed class LinkBegin: SignInResult() {
        data class Success(val ephemeralJwt: String): LinkBegin()
        data class NoDevicesAvailable(val message: UIMessage): LinkBegin()
        data class Failure(val message: UIMessage): LinkBegin()
    }

    sealed class LinkAuth: SignInResult() {
        class Success: LinkAuth()
        data class Failure(val message: UIMessage,
                           val exception: Exception): LinkAuth()
    }

    sealed class CreateSessionFromLink: SignInResult() {
        class Success: CreateSessionFromLink()
        data class Failure(val message: UIMessage,
                           val exception: Exception): CreateSessionFromLink()
    }
}
