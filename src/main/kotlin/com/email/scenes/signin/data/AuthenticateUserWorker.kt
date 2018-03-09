package com.email.scenes.signin.data

import android.accounts.NetworkErrorException
import com.email.R
import com.email.api.SignInAPILoader
import com.email.bgworker.BackgroundWorker
import com.email.db.SignInLocalDB
import com.email.utils.UIMessage
import com.github.kittinunf.result.Result
import org.json.JSONException

/**
 * Created by sebas on 2/28/18.
 */
class AuthenticateUserWorker(
        private val db: SignInLocalDB,
        private val apiClient: SignInAPIClient,
        private val username: String,
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
        val message = createErrorMessage(ex)
        return SignInResult.AuthenticateUser.Failure(message, ex)
    }

    override fun work(): SignInResult.AuthenticateUser? {
        val operationResult =  loader.authenticateUser(
                username = username,
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

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is NetworkErrorException ->
                UIMessage(resId = R.string.login_network_error_exception)
            is JSONException ->
                    UIMessage(resId = R.string.login_json_error_exception)
            else -> UIMessage(resId = R.string.login_fail_try_again_error_exception)
        }
    }
}
