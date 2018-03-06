package com.email.scenes.signup.data

import com.email.utils.UIMessage

/**
 * Created by sebas on 2/26/18.
 */

sealed class SignUpResult {

    sealed class RegisterUser: SignUpResult() {
        class Success: RegisterUser()
        data class Failure(
                val message: UIMessage,
                val exception: Exception): RegisterUser()
    }

    sealed class PostKeyBundle: SignUpResult() {
        class Success: PostKeyBundle()
        data class Failure(
                val message: UIMessage,
                val exception: Exception): PostKeyBundle()
    }
}

