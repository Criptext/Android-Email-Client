package com.email.scenes.signup.data

import android.accounts.NetworkErrorException
import com.email.R
import com.email.api.KeyServerClient
import com.email.api.KeyServerLoader
import com.email.api.PreKeyBundleShareData
import com.email.bgworker.BackgroundWorker
import com.email.utils.UIMessage
import com.github.kittinunf.result.Result
import org.json.JSONException

/**
 * Created by sebas on 3/5/18.
 */

class PostKeyBundleWorker(
        private val apiClient: KeyServerClient,
        private val completeBundle: PreKeyBundleShareData.UploadBundle,
        override val publishFn: (SignUpResult.PostKeyBundle) -> Unit)
    : BackgroundWorker<SignUpResult.PostKeyBundle> {

    override val canBeParallelized = false
    private val loader = KeyServerLoader(
            keyServerClient = apiClient)

    override fun catchException(ex: Exception): SignUpResult.PostKeyBundle {
        val message = createErrorMessage(ex)
        return SignUpResult.PostKeyBundle.Failure(message, ex)
    }

    override fun work(): SignUpResult.PostKeyBundle? {
        val operationResult =  loader.postKeyBundle(
                completeBundle = completeBundle
        )
        return when(operationResult) {
            is Result.Success -> {
                SignUpResult.PostKeyBundle.Success()
            }
            is Result.Failure -> {
                SignUpResult.PostKeyBundle.Failure(
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
