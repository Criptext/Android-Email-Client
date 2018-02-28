package com.email.api

import com.email.db.SignInLocalDB
import com.email.db.models.User
import com.email.scenes.signin.data.SignInAPIClient
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError

/**
 * Created by sebas on 2/28/18.
 */

class SignInAPILoader(private val localDB: SignInLocalDB,
                      private val signInAPIClient: SignInAPIClient) {

    fun authenticateUser(user: User,
                     password: String,
                     deviceId: Int):
            Result<String, Exception> {
        val operationResult = registerUserOperation(
                user = user,
                password = password,
                deviceId = deviceId)
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return operationResult
    }

    private fun registerUserOperation(
            user: User,
            password: String,
            deviceId: Int):
            Result<String, Exception> {
        return signInAPIClient.authenticateUser(
                user = user,
                password = password,
                deviceId = deviceId)
    }
}
