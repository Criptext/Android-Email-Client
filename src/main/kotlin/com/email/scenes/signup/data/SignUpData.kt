package com.email.scenes.signup.data

import com.email.db.models.User

/**
 * Created by sebas on 2/27/18.
 */


//TODO: THIS CLASS IS NOT BEING USED YET.
object SignUpData {

    sealed class RegisterUser {

        class Success(
                val user: User,
                val password: String,
                val recoveryEmail: String?
                ): RegisterUser()

        class Failure(
                val message: String
        ): RegisterUser()
    }
}
