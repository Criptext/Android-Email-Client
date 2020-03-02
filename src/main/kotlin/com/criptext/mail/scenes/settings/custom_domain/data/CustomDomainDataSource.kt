package com.criptext.mail.scenes.settings.custom_domain.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.custom_domain.workers.DeleteDomainWorker
import com.criptext.mail.scenes.settings.custom_domain.workers.LoadDomainWorker
import com.criptext.mail.scenes.settings.custom_domain_entry.workers.CheckDomainAvailableWorker
import com.criptext.mail.scenes.settings.custom_domain_entry.workers.RegisterDomainWorker
import com.criptext.mail.scenes.settings.recovery_email.workers.ChangeRecoveryEmailWorker
import com.criptext.mail.scenes.settings.recovery_email.workers.ResendLinkWorker

class CustomDomainDataSource(
        private val db: AppDatabase,
        private val storage: KeyValueStorage,
        var activeAccount: ActiveAccount,
        private val httpClient: HttpClient,
        override val runner: WorkRunner)
    : BackgroundWorkManager<CustomDomainRequest, CustomDomainResult>(){

    override fun createWorkerFromParams(params: CustomDomainRequest,
                                        flushResults: (CustomDomainResult) -> Unit): BackgroundWorker<*> {

        return when(params){
            is CustomDomainRequest.DeleteDomain-> DeleteDomainWorker(
                    accountDao = db.accountDao(),
                    storage = storage,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    domain = params.domain,
                    domainDao = db.customDomainDao(),
                    position = params.position,
                    aliasDao = db.aliasDao(),
                    publishFn = flushResults
            )
            is CustomDomainRequest.LoadDomain-> LoadDomainWorker(
                    accountDao = db.accountDao(),
                    storage = storage,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    domainDao = db.customDomainDao(),
                    publishFn = flushResults
            )
        }
    }
}