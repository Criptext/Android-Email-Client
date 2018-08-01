package com.criptext.mail.scenes.signup.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.github.kittinunf.result.Result

/**
 * Created by gabriel on 5/16/18.
 */

class CheckUsernameAvailabilityWorker(val httpClient: HttpClient,
                                      private val username: String,
                                      override val publishFn: (SignUpResult) -> Unit)
    : BackgroundWorker<SignUpResult.CheckUsernameAvailability> {

    private val apiClient = SignUpAPIClient(httpClient)

    override val canBeParallelized = true

    override fun catchException(ex: Exception): SignUpResult.CheckUsernameAvailability {
        return SignUpResult.CheckUsernameAvailability.Failure()
    }

    override fun work(reporter: ProgressReporter<SignUpResult.CheckUsernameAvailability>)
            : SignUpResult.CheckUsernameAvailability? {
        val result = Result.of { apiClient.isUsernameAvailable(username) }

        return when (result) {
            is Result.Success -> SignUpResult.CheckUsernameAvailability.Success(isAvailable = true)
            is Result.Failure -> {
                val error = result.error
                if (error is ServerErrorException && error.errorCode == 400)
                    SignUpResult.CheckUsernameAvailability.Success(isAvailable = false)
                else
                    SignUpResult.CheckUsernameAvailability.Failure()
            }
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}