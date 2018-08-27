package com.criptext.mail.scenes.settings.recovery_email.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError
import java.io.File



/**
 * Created by danieltigse on 28/06/18.
 */

class ResendLinkWorker(
        private val storage: KeyValueStorage,
        httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        override val publishFn: (
                RecoveryEmailResult.ResendConfirmationLink) -> Unit)
    : BackgroundWorker<RecoveryEmailResult.ResendConfirmationLink> {

    override val canBeParallelized = true
    private val apiClient = RecoveryEmailAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): RecoveryEmailResult.ResendConfirmationLink {
        return RecoveryEmailResult.ResendConfirmationLink.Failure()
    }

    override fun work(reporter: ProgressReporter<RecoveryEmailResult.ResendConfirmationLink>): RecoveryEmailResult.ResendConfirmationLink? {
        val deleteOperation = Result.of {apiClient.putResendLink()}
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when (deleteOperation){
            is Result.Success -> {
                RecoveryEmailResult.ResendConfirmationLink.Success()
            }
            is Result.Failure -> {
                RecoveryEmailResult.ResendConfirmationLink.Failure()
            }
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

}
