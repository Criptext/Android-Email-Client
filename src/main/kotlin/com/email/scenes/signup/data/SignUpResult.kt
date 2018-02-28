package com.email.scenes.signup.data

/**
 * Created by sebas on 2/26/18.
 */

sealed class SignUpResult {

    sealed class RegisterUser: SignUpResult() {
        class Success: RegisterUser()
        data class Failure(val message: String): RegisterUser()
    }
}

