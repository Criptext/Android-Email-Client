package com.email.api

import com.email.db.SignUpLocalDB
import com.email.scenes.signup.IncompleteAccount
import com.email.scenes.signup.data.SignUpAPIClient
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError
import java.lang.Exception

/**
 * Created by sebas on 2/27/18.
 */
class SignUpAPILoader(private val localDB: SignUpLocalDB,
                      private val signUpAPIClient: SignUpAPIClient) {

    fun registerUser(account: IncompleteAccount,
                     recipientId: String,
                     keybundle: PreKeyBundleShareData.UploadBundle
                     ):
            Result<String, Exception>{
        val operationResult = registerUserOperation(
                account = account,
                recipientId = recipientId,
                keyBundle = keybundle)
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return operationResult
    }

    private fun registerUserOperation(
            account: IncompleteAccount,
            recipientId: String,
            keyBundle: PreKeyBundleShareData.UploadBundle):
            Result<String, Exception> {
        return Result.of {
            val message = signUpAPIClient.createUser(
                    account = account,
                    recipientId =  recipientId,
                    keybundle = keyBundle)
            message
        }
    }

}
