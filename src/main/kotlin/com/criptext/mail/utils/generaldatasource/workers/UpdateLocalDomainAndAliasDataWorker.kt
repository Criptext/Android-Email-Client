package com.criptext.mail.utils.generaldatasource.workers

import android.content.res.Resources
import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.dao.AliasDao
import com.criptext.mail.db.dao.CustomDomainDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Alias
import com.criptext.mail.scenes.settings.aliases.data.AliasData
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result


class UpdateLocalDomainAndAliasDataWorker(private val customDomainDao: CustomDomainDao,
                                          private val aliasDao: AliasDao,
                                          private val activeAccount: ActiveAccount,
                                          private val customDomains: List<String>,
                                          private val aliasData: List<AliasData>,
                                          override val publishFn: (GeneralResult.UpdateLocalDomainAndAliasData) -> Unit
                          ) : BackgroundWorker<GeneralResult.UpdateLocalDomainAndAliasData> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): GeneralResult.UpdateLocalDomainAndAliasData {
        return when(ex){
            is Resources.NotFoundException -> GeneralResult.UpdateLocalDomainAndAliasData.Failure(UIMessage(R.string.error_sync_contact))
            else -> GeneralResult.UpdateLocalDomainAndAliasData.Failure(UIMessage(R.string.already_sync_contact))
        }
    }

    override fun work(reporter: ProgressReporter<GeneralResult.UpdateLocalDomainAndAliasData>)
            : GeneralResult.UpdateLocalDomainAndAliasData? {
        val operation = Result.of {
            val dbAliases = aliasDao.getAll(activeAccount.id)
            val dbCustomDomains = customDomainDao.getAll(activeAccount.id)

            val newAliases = aliasData.filter { it.rowId !in dbAliases.map { dbAlias -> dbAlias.rowId } }
            val deletedAliases = dbAliases.filter { it.rowId !in aliasData.map { serverAlias -> serverAlias.rowId } }
            aliasDao.deleteAll(deletedAliases)
            aliasDao.insertAll(newAliases.map { Alias(0, rowId = it.rowId,
                    accountId = activeAccount.id, domain = it.domain, active = it.isActive, name = it.name) })

            val newDomains = customDomains.filter { it !in dbCustomDomains.map { dbDomain -> dbDomain.name } }
            val deletedDomains = dbCustomDomains.filter { it.rowId !in aliasData.map { serverAlias -> serverAlias.rowId } }
            customDomainDao.deleteAll(deletedDomains)
        }
        return when (operation){
            is Result.Success -> {
                GeneralResult.UpdateLocalDomainAndAliasData.Success()
            }
            is Result.Failure -> {
                catchException(operation.error)
            }
        }
    }

    override fun cancel() {
        TODO("not implemented")
    }
}