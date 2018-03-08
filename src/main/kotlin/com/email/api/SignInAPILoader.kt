package com.email.api

import com.email.db.SignInLocalDB
import com.email.scenes.signin.data.SignInAPIClient
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError

/**
 * Created by sebas on 2/28/18.
 */

class SignInAPILoader(private val localDB: SignInLocalDB,
                      private val signInAPIClient: SignInAPIClient) {

    fun authenticateUser(username: String,
                         password: String,
                         deviceId: Int):
            Result<String, Exception> {
        val operationResult = authenticateUserOperation(
                username = username,
                password = password,
                deviceId = deviceId)
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return operationResult
    }

    private fun authenticateUserOperation(
            username: String,
            password: String,
            deviceId: Int):
            Result<String, Exception> {
        return Result.of {
            val message = signInAPIClient.authenticateUser(
                    username = username,
                    password = password,
                    deviceId = deviceId)
            message
        }
    }
}
