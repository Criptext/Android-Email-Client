package com.email.api

import com.email.db.SignUpLocalDB
import com.email.db.models.User
import com.email.scenes.signup.data.RegisterUser
import com.email.scenes.signup.data.SignUpAPIClient
import com.email.scenes.signup.data.SignUpResult
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import com.github.kittinunf.result.mapError
import java.lang.Exception

/**
 * Created by sebas on 2/27/18.
 */
class SignUpAPILoader(private val localDB: SignUpLocalDB,
                      private val signUpAPIClient: SignUpAPIClient) {

    fun registerUser(user: User,
                             password: String,
                             recoveryEmail: String?): SignUpResult.RegisterUser {
        // TODO register User Operation
        val operationCode = registerUserOperation(
                user = user,
                password = password,
                recoveryEmail = recoveryEmail)
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when(operationCode) {
            is Result.Success -> {
                SignUpResult.RegisterUser.Success(operationCode.value)
            }
            is Result.Failure -> {
                SignUpResult.RegisterUser.Failure(operationCode.error.toString())
            }
        }
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
