package com.criptext.mail.scenes.settings.workers

import android.content.res.Resources
import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.CustomDomainDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.data.SettingsAPIClient
import com.criptext.mail.scenes.settings.data.SettingsResult
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError


class CheckCustomDomainWorker(val httpClient: HttpClient,
                              val activeAccount: ActiveAccount,
                              private val customDomainDao: CustomDomainDao,
                              override val publishFn: (SettingsResult) -> Unit)
    : BackgroundWorker<SettingsResult.CheckCustomDomain> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): SettingsResult.CheckCustomDomain {
        return SettingsResult.CheckCustomDomain.Failure(createErrorMessage(ex))
    }

    override fun work(reporter: ProgressReporter<SettingsResult.CheckCustomDomain>): SettingsResult.CheckCustomDomain? {
        val customDomain = customDomainDao.getAll()

        return if(customDomain.isEmpty())
            catchException(Resources.NotFoundException())
        else {
            SettingsResult.CheckCustomDomain.Success(customDomain.first())
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        UIMessage(R.string.local_error, arrayOf(ex.toString()))
    }

}