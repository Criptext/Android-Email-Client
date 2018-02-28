package com.email.api

import com.email.db.SignUpLocalDB
import com.email.db.models.User
import com.email.scenes.signup.data.SignUpAPIClient
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError
import java.lang.Exception

/**
 * Created by sebas on 2/27/18.
 */
class SignUpAPILoader(private val localDB: SignUpLocalDB,
                      private val signUpAPIClient: SignUpAPIClient) {

    fun registerUser(user: User,
                             password: String,
                             recoveryEmail: String?):
            Result<String, Exception>{
        val operationResult = registerUserOperation(
                user = user,
                password = password,
                recoveryEmail = recoveryEmail)
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return operationResult
    }

    private fun registerUserOperation(
            user: User,
            password: String,
            recoveryEmail: String?):
            Result<String, Exception> {
        return signUpAPIClient.createUser(
                user = user,
                password = password,
                recoveryEmail = recoveryEmail)
    }
}
