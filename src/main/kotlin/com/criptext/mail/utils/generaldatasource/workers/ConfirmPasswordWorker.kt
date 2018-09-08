package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.sha256
import com.github.kittinunf.result.Result

class ConfirmPasswordWorker(private val password: String,
                            private val activeAccount: ActiveAccount,
                            private val httpClient: HttpClient,
                            override val publishFn: (GeneralResult.ConfirmPassword) -> Unit
                          ) : BackgroundWorker<GeneralResult.ConfirmPassword> {

    override val canBeParallelized = false

    private val apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): GeneralResult.ConfirmPassword {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(reporter: ProgressReporter<GeneralResult.ConfirmPassword>)
            : GeneralResult.ConfirmPassword? {

        val confirmPasswordOperation = Result.of {
            apiClient.postUnlockDevice(password.sha256())
        }
        return when (confirmPasswordOperation){
            is Result.Success -> {
                GeneralResult.ConfirmPassword.Success()
            }
            is Result.Failure -> {
                GeneralResult.ConfirmPassword.Failure()
            }
        }
    }

    override fun cancel() {
        TODO("not implemented")
    }
}