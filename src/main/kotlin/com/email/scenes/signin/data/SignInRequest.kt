package com.email.scenes.signin.data

import com.email.db.models.User

/**
 * Created by sebas on 2/28/18.
 */

sealed class SignInRequest{
    class AuthenticateUser(val user: User,
                           val password: String,
                           val deviceId: Int
    ): SignInRequest()
}
