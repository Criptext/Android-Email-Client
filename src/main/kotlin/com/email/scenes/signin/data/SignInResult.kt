package com.email.scenes.signin.data

import com.email.utils.UIMessage

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
}
