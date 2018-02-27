package com.email.scenes.signup.data

import com.email.bgworker.BackgroundWorker
import com.email.db.UserDB
import com.email.db.models.User

/**
 * Created by sebas on 2/26/18.
 */

class RegisterUserWorker(
        private val db: UserDB,
        private val apiClient: SignUpAPIClient,
        private val user: User,
        override val publishFn: (SignUpResult.RegisterUser) -> Unit)
    : BackgroundWorker<SignUpResult.RegisterUser> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): SignUpResult.RegisterUser {
        val message = "Unexpected error: " + ex.message
        return SignUpResult.RegisterUser.Failure(message)
    }

    override fun work(): SignUpResult.RegisterUser? {
        return apiClient.createUser(username = user.nickname,
                password = "testing",
                name = user.name)
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

