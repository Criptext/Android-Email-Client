package com.email.scenes.signup.data

import com.email.db.models.User

/**
 * Created by sebas on 2/27/18.
 */

sealed class RegisterUser {
    class Success(
            val user: User,
            val password: String,
            val message: String,
            val recoveryEmail: String?
            ): RegisterUser()

    class Failure(
            val user: User,
            val message: String): RegisterUser()

    data class RegisterUserData(
            val user: User,
            val password: String,
            val message: String,
            val recoveryEmail: String?
    )
}
