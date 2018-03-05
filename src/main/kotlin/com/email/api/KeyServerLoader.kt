package com.email.api

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError

/**
 * Created by sebas on 3/5/18.
 */

class KeyServerLoader(private val keyServerClient: KeyServerClient) {

    fun postKeyBundle(completeBundle: PreKeyBundleShareData.UploadBundle):
            Result<String, Exception> {
        val operationResult = postKeyBundleOperation(completeBundle)
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return operationResult
    }

    private fun postKeyBundleOperation(
            completeBundle: PreKeyBundleShareData.UploadBundle):
            Result<String, Exception> {
        return Result.of {
            val message = keyServerClient.postKeyBundle(
                    completeBundle
                   )
            message
        }
    }

}
