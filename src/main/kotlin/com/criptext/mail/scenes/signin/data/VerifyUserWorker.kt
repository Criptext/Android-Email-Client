package com.criptext.mail.scenes.signin.data

import android.accounts.NetworkErrorException
import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.SignInLocalDB
import com.criptext.mail.utils.UIMessage
import org.json.JSONException

/**
 * Created by sebas on 3/8/18.
 */

class VerifyUserWorker(
        private val db: SignInLocalDB,
        private val username: String,
        override val publishFn: (SignInResult.VerifyUser) -> Unit)
    : BackgroundWorker<SignInResult.VerifyUser> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): SignInResult.VerifyUser {
        val message = createErrorMessage(ex)
        return SignInResult.VerifyUser.Failure(message, ex)
    }

    override fun work(reporter: ProgressReporter<SignInResult.VerifyUser>): SignInResult.VerifyUser? {
        val userExists = db.accountExistsLocally(username = username)
        if(userExists) return SignInResult.VerifyUser.Success()

        val error = Exception()
        return SignInResult.VerifyUser.Failure(createErrorMessage(error), error)
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
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
