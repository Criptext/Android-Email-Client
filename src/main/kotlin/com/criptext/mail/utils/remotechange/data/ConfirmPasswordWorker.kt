package com.criptext.mail.utils.remotechange.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.utils.sha256
import com.github.kittinunf.result.Result

class ConfirmPasswordWorker(private val password: String,
                            private val activeAccount: ActiveAccount,
                            private val httpClient: HttpClient,
                            override val publishFn: (RemoteChangeResult.ConfirmPassword) -> Unit
                          ) : BackgroundWorker<RemoteChangeResult.ConfirmPassword> {

    override val canBeParallelized = false

    private val apiClient = RemoteChangeAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): RemoteChangeResult.ConfirmPassword {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(reporter: ProgressReporter<RemoteChangeResult.ConfirmPassword>)
            : RemoteChangeResult.ConfirmPassword? {

        val confirmPasswordOperation = Result.of {
            apiClient.postUnlockDevice(password.sha256())
        }
        return when (confirmPasswordOperation){
            is Result.Success -> {
                RemoteChangeResult.ConfirmPassword.Success()
            }
            is Result.Failure -> {
                RemoteChangeResult.ConfirmPassword.Failure()
            }
        }
    }

    override fun cancel() {
        TODO("not implemented")
    }
}