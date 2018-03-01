package com.email.scenes.signup.data

import android.accounts.NetworkErrorException
import android.support.v4.content.res.ResourcesCompat
import com.email.R
import com.email.api.ServerErrorException
import com.email.api.SignUpAPILoader
import com.email.bgworker.BackgroundWorker
import com.email.db.SignUpLocalDB
import com.email.db.models.User
import com.email.utils.UIMessage
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

        val message = createErrorMessage(ex)
        return SignUpResult.RegisterUser.Failure(message, ex)
    }

    override fun work(): SignUpResult.RegisterUser? {
        val operationResult =  loader.registerUser(
                user = user,
                password = password,
                recoveryEmail = recoveryEmail
        )
        return when(operationResult) {
            is Result.Success -> {
                SignUpResult.RegisterUser.Success()
            }
            is Result.Failure -> {
                SignUpResult.RegisterUser.Failure(
                        message = createErrorMessage(operationResult.error),
                        exception = operationResult.error)
            }
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is NetworkErrorException -> UIMessage(resId = R.string.network_error_exception)
            is JSONException -> UIMessage(resId = R.string.json_error_exception)
            is ServerErrorException -> {
                if(ex.errorCode == 400) {
                    UIMessage(resId = R.string.duplicate_name_error_exception)
                } else {
                    UIMessage(resId = R.string.server_error_exception)
                }
            }
            else -> UIMessage(resId = R.string.fail_register_try_again_error_exception)
        }
    }
}

