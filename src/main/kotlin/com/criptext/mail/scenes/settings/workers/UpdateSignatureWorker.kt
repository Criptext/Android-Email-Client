package com.criptext.mail.scenes.settings.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.data.SettingsAPIClient
import com.criptext.mail.scenes.settings.data.SettingsResult
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError


class UpdateSignatureWorker(val httpClient: HttpClient,
                            val activeAccount: ActiveAccount,
                            private val signature: String,
                            private val accountDao: AccountDao,
                            override val publishFn: (SettingsResult) -> Unit)
    : BackgroundWorker<SettingsResult.UpdateSignature> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): SettingsResult.UpdateSignature {
        return SettingsResult.UpdateSignature.Failure(createErrorMessage(ex))
    }

    override fun work(reporter: ProgressReporter<SettingsResult.UpdateSignature>): SettingsResult.UpdateSignature? {
        val result = Result.of {
            accountDao.updateSignature(activeAccount.id, signature)
        }

        return when (result) {
            is Result.Success -> SettingsResult.UpdateSignature.Success()
            is Result.Failure -> catchException(result.error)
        }

    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        UIMessage(R.string.local_error, arrayOf(ex.toString()))
    }

}