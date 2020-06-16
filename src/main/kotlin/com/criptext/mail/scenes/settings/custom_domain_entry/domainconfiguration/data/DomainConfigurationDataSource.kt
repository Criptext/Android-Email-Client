package com.criptext.mail.scenes.settings.custom_domain_entry.domainconfiguration.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.custom_domain_entry.domainconfiguration.workers.DomainValidationWorker
import com.criptext.mail.scenes.settings.custom_domain_entry.domainconfiguration.workers.GetMXRecordsWorker

class DomainConfigurationDataSource(
        private val db: AppDatabase,
        private val storage: KeyValueStorage,
        var activeAccount: ActiveAccount,
        private val httpClient: HttpClient,
        override val runner: WorkRunner)
    : BackgroundWorkManager<DomainConfigurationRequest, DomainConfigurationResult>(){

    override fun createWorkerFromParams(params: DomainConfigurationRequest,
                                        flushResults: (DomainConfigurationResult) -> Unit): BackgroundWorker<*> {

        return when(params){
            is DomainConfigurationRequest.GetMXRecords -> GetMXRecordsWorker(
                    accountDao = db.accountDao(),
                    storage = storage,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    domain = params.domain,
                    publishFn = flushResults
            )
            is DomainConfigurationRequest.ValidateDomain -> DomainValidationWorker(
                    accountDao = db.accountDao(),
                    storage = storage,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    domain = params.domain,
                    customDomainDao = db.customDomainDao(),
                    publishFn = flushResults
            )
        }
    }
}