package com.email.scenes.signup.data

import android.accounts.NetworkErrorException
import com.email.api.SignUpAPILoader
import com.email.bgworker.BackgroundWorker
import com.email.db.SignUpLocalDB
import com.email.db.models.User
import com.github.kittinunf.result.Result
import org.json.JSONException

/**
 * Created by sebas on 2/26/18.
 */

class RegisterUserWorker(
        private val db: SignUpLocalDB,
        private val apiClient: SignUpAPIClient,
        private val user: User,
        private val password: String,
        private val recoveryEmail: String?,
        override val publishFn: (SignUpResult.RegisterUser) -> Unit)
    : BackgroundWorker<SignUpResult.RegisterUser> {

    override val canBeParallelized = false
    private val loader = SignUpAPILoader(
            localDB = db,
            signUpAPIClient = apiClient
    )

    override fun catchException(ex: Exception): SignUpResult.RegisterUser {
        val message = "Unexpected error: " + ex.message
        return SignUpResult.RegisterUser.Failure(message)
    }

    override fun work(): SignUpResult.RegisterUser? {
        val operationResult =  loader.registerUser(
                user = user,
                password = password,
                recoveryEmail = recoveryEmail
        )
        return when(operationResult) {
            is Result.Success -> {
                SignUpResult.RegisterUser.Success(operationResult.value)
            }
            is Result.Failure -> {
                SignUpResult.RegisterUser.Failure(createErrorMessage(operationResult.error))
            }
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val createErrorMessage: (ex: Exception) -> String = { ex ->
        when (ex) {
            is NetworkErrorException -> "Failed to register user. " +
                    "Please check your internet connection."
            is JSONException -> "Failed to register user. Invalid server response."
            else -> "Failed to register user. Please try again later."
        }
    }
}

