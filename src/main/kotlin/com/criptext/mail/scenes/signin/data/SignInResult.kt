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
        class Success: ForgotPassword()
        class Failure: ForgotPassword()
    }
}
