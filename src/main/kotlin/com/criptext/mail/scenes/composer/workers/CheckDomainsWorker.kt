package com.criptext.mail.scenes.composer.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.composer.data.ComposerAPIClient
import com.criptext.mail.scenes.composer.data.ComposerResult
import com.criptext.mail.scenes.composer.data.ContactDomainCheckData
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result

class CheckDomainsWorker(
        private val emails: List<String>,
        httpClient: HttpClient,
        activeAccount: ActiveAccount,
        override val publishFn: (ComposerResult.CheckDomain) -> Unit)
    : BackgroundWorker<ComposerResult.CheckDomain> {

    private val apiClient = ComposerAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = true

    override fun catchException(ex: Exception): ComposerResult.CheckDomain {
        if(ex is ServerErrorException)
            return ComposerResult.CheckDomain.Failure(UIMessage(R.string.domain_validation_error, arrayOf(ex.errorCode)))
        return ComposerResult.CheckDomain.Failure(UIMessage(R.string.domain_validation_error, arrayOf(-1)))
    }

    override fun work(reporter: ProgressReporter<ComposerResult.CheckDomain>)
            : ComposerResult.CheckDomain? {
        val operation = Result.of {
            apiClient.getIsSecureDomain(emails.map { EmailAddressUtils.extractEmailAddressDomain(it) }.distinct())
        }
        return when(operation){
            is Result.Success -> {
                ComposerResult.CheckDomain.Success(ContactDomainCheckData.fromJSON(operation.value.body))
            }
            is Result.Failure -> {
                catchException(operation.error)
            }
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}

