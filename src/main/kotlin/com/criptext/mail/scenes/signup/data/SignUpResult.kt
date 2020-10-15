package com.criptext.mail.scenes.signup.data

import com.criptext.mail.utils.UIMessage

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

    sealed class CheckUsernameAvailability: SignUpResult() {
        data class Success(val isAvailable: Boolean): CheckUsernameAvailability()
        class Failure: CheckUsernameAvailability()
    }

    sealed class CheckRecoveryEmailAvailability: SignUpResult() {
        class Success: CheckRecoveryEmailAvailability()
        data class Failure(val errorMessage: UIMessage): CheckRecoveryEmailAvailability()
    }

    sealed class GetCaptcha: SignUpResult() {
        data class Success(val captchaKey: String, val captcha: String): GetCaptcha()
        data class Failure(val message: UIMessage): GetCaptcha()
    }
}

