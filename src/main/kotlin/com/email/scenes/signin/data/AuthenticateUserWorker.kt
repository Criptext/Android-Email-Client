package com.email.scenes.signin.data

import android.accounts.NetworkErrorException
import com.email.api.DuplicateUsernameException
import com.email.api.SignInAPILoader
import com.email.bgworker.BackgroundWorker
import com.email.db.SignInLocalDB
import com.email.db.models.User
import com.github.kittinunf.result.Result
import org.json.JSONException

/**
 * Created by sebas on 2/28/18.
 */
class AuthenticateUserWorker(
        private val db: SignInLocalDB,
        private val apiClient: SignInAPIClient,
        private val user: User,
        private val password: String,
        private val deviceId: Int,
        override val publishFn: (SignInResult.AuthenticateUser) -> Unit)
    : BackgroundWorker<SignInResult.AuthenticateUser> {

    override val canBeParallelized = false
    private val loader = SignInAPILoader(
            localDB = db,
            signInAPIClient = apiClient
    )

    override fun catchException(ex: Exception): SignInResult.AuthenticateUser {
        val message = "Unexpected error: " + ex.message
        return SignInResult.AuthenticateUser.Failure(message, ex)
    }

    override fun work(): SignInResult.AuthenticateUser? {
        val operationResult =  loader.authenticateUser(
                user = user,
                password = password,
                deviceId = deviceId
        )
        return when(operationResult) {
            is Result.Success -> {
                SignInResult.AuthenticateUser.Success()
            }
            is Result.Failure -> {
                SignInResult.AuthenticateUser.Failure(
                        message = createErrorMessage(operationResult.error),
                        exception = operationResult.error)
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
            is JSONException -> "Failed to register user. " +
                    "Invalid server response."
            is DuplicateUsernameException -> "Failed to register user. " +
                    "Username already exists"
            else -> "Failed to register user. Please try again later."
        }
    }
}
