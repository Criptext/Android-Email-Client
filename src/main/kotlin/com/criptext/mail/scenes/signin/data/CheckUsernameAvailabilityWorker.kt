package com.criptext.mail.scenes.signin.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.scenes.signup.data.SignUpAPIClient
import com.criptext.mail.utils.ServerCodes
import com.github.kittinunf.result.Result

/**
 * Created by gabriel on 5/16/18.
 */

class CheckUsernameAvailabilityWorker(val httpClient: HttpClient,
                                      private val username: String,
                                      override val publishFn: (SignInResult) -> Unit)
    : BackgroundWorker<SignInResult.CheckUsernameAvailability> {

    private val apiClient = SignUpAPIClient(httpClient)

    override val canBeParallelized = true

    override fun catchException(ex: Exception): SignInResult.CheckUsernameAvailability {
        return SignInResult.CheckUsernameAvailability.Failure()
    }

    override fun work(reporter: ProgressReporter<SignInResult.CheckUsernameAvailability>): SignInResult.CheckUsernameAvailability? {
        val result = Result.of { apiClient.isUsernameAvailable(username) }

        return when (result) {
            is Result.Success -> SignInResult.CheckUsernameAvailability.Success(userExists = false, username = username)
            is Result.Failure -> {
                val error = result.error
                if (error is ServerErrorException && error.errorCode == ServerCodes.BadRequest)
                    SignInResult.CheckUsernameAvailability.Success(userExists = true, username = username)
                else
                    SignInResult.CheckUsernameAvailability.Failure()
            }
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}