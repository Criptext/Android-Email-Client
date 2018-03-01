package com.email.mocks.api

import com.email.api.DuplicateUsernameException
import com.email.db.models.User
import com.email.scenes.signup.data.SignUpAPIClient
import com.github.kittinunf.result.Result

/**
 * Created by sebas on 3/1/18.
 */

class MockSignUpAPIClient(private val registeredUsers: List<String>): SignUpAPIClient {
    override fun createUser(
            user: User,
            password: String,
            recoveryEmail: String?): Result<String, Exception> {
        return Result.of {
            if(user.nickname in registeredUsers) {
                throw DuplicateUsernameException(422)
            } else {
                ""
            }
        }

    }
}
