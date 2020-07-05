package com.criptext.mail.scenes.settings.custom_domain_entry.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.custom_domain_entry.workers.CheckDomainAvailableWorker
import com.criptext.mail.scenes.settings.custom_domain_entry.workers.RegisterDomainWorker

class CustomDomainEntryDataSource(
        private val db: AppDatabase,
        private val storage: KeyValueStorage,
        var activeAccount: ActiveAccount,
        private val httpClient: HttpClient,
        override val runner: WorkRunner)
    : BackgroundWorkManager<CustomDomainEntryRequest, CustomDomainEntryResult>(){

    override fun createWorkerFromParams(params: CustomDomainEntryRequest,
                                        flushResults: (CustomDomainEntryResult) -> Unit): BackgroundWorker<*> {

        return when(params){
            is CustomDomainEntryRequest.CheckDomainAvailability -> CheckDomainAvailableWorker(
                    accountDao = db.accountDao(),
                    storage = storage,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    domain = params.domain,
                    customDomainDao = db.customDomainDao(),
                    publishFn = flushResults
            )
            is CustomDomainEntryRequest.RegisterDomain -> RegisterDomainWorker(
                    accountDao = db.accountDao(),
                    storage = storage,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    customDomainDao = db.customDomainDao(),
                    domain = params.domain,
                    publishFn = flushResults
            )
        }
    }
}