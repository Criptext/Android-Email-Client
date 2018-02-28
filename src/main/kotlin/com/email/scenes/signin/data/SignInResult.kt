package com.email.scenes.signin.data

/**
 * Created by sebas on 2/28/18.
 */

sealed class SignInResult {

    sealed class AuthenticateUser: SignInResult() {
        class Success: AuthenticateUser()
        data class Failure(
                val message: String,
                val exception: Exception): AuthenticateUser()
    }
}
