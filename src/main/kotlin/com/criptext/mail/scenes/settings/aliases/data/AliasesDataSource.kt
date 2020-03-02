package com.criptext.mail.scenes.settings.aliases.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.aliases.workers.AddAliasWorker
import com.criptext.mail.scenes.settings.aliases.workers.DeleteAliasWorker
import com.criptext.mail.scenes.settings.aliases.workers.EnableAliasWorker
import com.criptext.mail.scenes.settings.aliases.workers.LoadAliasesWorker
import com.criptext.mail.scenes.settings.custom_domain.workers.DeleteDomainWorker
import com.criptext.mail.scenes.settings.custom_domain.workers.LoadDomainWorker
import com.criptext.mail.scenes.settings.custom_domain_entry.workers.CheckDomainAvailableWorker
import com.criptext.mail.scenes.settings.custom_domain_entry.workers.RegisterDomainWorker
import com.criptext.mail.scenes.settings.recovery_email.workers.ChangeRecoveryEmailWorker
import com.criptext.mail.scenes.settings.recovery_email.workers.ResendLinkWorker

class AliasesDataSource(
        private val db: AppDatabase,
        private val storage: KeyValueStorage,
        var activeAccount: ActiveAccount,
        private val httpClient: HttpClient,
        override val runner: WorkRunner)
    : BackgroundWorkManager<AliasesRequest, AliasesResult>(){

    override fun createWorkerFromParams(params: AliasesRequest,
                                        flushResults: (AliasesResult) -> Unit): BackgroundWorker<*> {

        return when(params){
            is AliasesRequest.LoadAliases-> LoadAliasesWorker(
                    accountDao = db.accountDao(),
                    storage = storage,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    customDomainDao = db.customDomainDao(),
                    aliasDao = db.aliasDao(),
                    publishFn = flushResults
            )
            is AliasesRequest.AddAlias-> AddAliasWorker(
                    accountDao = db.accountDao(),
                    storage = storage,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    alias = params.alias,
                    domain = params.domain,
                    aliasDao = db.aliasDao(),
                    publishFn = flushResults
            )
            is AliasesRequest.DeleteAlias-> DeleteAliasWorker(
                    accountDao = db.accountDao(),
                    storage = storage,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    position = params.position,
                    domain = params.domain,
                    alias = params.alias,
                    aliasDao = db.aliasDao(),
                    publishFn = flushResults
            )
            is AliasesRequest.EnableAlias-> EnableAliasWorker(
                    accountDao = db.accountDao(),
                    storage = storage,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    alias = params.alias,
                    domain = params.domain,
                    enable = params.enable,
                    position = params.position,
                    aliasDao = db.aliasDao(),
                    publishFn = flushResults
            )
        }
    }
}