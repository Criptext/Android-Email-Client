package com.criptext.mail.scenes.composer.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.PeerEventsApiHandler
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.ComposerLocalDB
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.EmailDao
import com.criptext.mail.db.dao.PendingEventDao
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.scenes.composer.data.ComposerAPIClient
import com.criptext.mail.scenes.composer.data.ComposerInputData
import com.criptext.mail.scenes.composer.data.ComposerResult
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.utils.AccountUtils
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.peerdata.PeerReadEmailData
import com.github.kittinunf.result.Result
import org.json.JSONObject


class SwitchActiveAccountWorker(private val db: ComposerLocalDB,
                                private val activeAccount: ActiveAccount,
                                private val oldAccountAddress: String,
                                private val newAccountAddress: String,
                                private val storage: KeyValueStorage,
                                override val publishFn: (ComposerResult.SwitchActiveAccount) -> Unit
                       ) : BackgroundWorker<ComposerResult.SwitchActiveAccount> {

    override val canBeParallelized = false


    override fun catchException(ex: Exception): ComposerResult.SwitchActiveAccount {
        return ComposerResult.SwitchActiveAccount.Failure(createErrorMessage(ex))
    }

    override fun work(reporter: ProgressReporter<ComposerResult.SwitchActiveAccount>)
            : ComposerResult.SwitchActiveAccount? {
        val result = Result.of {
            val account = getTrueAccount(newAccountAddress) ?: throw Exception()
            db.setActiveAccount(account.id)
            AccountUtils.setUserAsActiveAccount(account, storage)
        }

        return when (result) {
            is Result.Success -> {
                ComposerResult.SwitchActiveAccount.Success(result.value)
            }
            is Result.Failure -> {
                catchException(result.error)
            }
        }
    }

    private fun getTrueAccount(emailAddress: String): Account? {
        val domain = EmailAddressUtils.extractEmailAddressDomain(emailAddress)
        val recipientId = EmailAddressUtils.extractRecipientIdFromAddress(emailAddress, domain)
        val account = db.accountDao.getAccount(recipientId, domain)
        if(account != null)
            return account
        val aliases = db.aliasDao.getAll()
        aliases.filter { it.active }.forEach { alias ->
            val aliasEmail = alias.name.plus("@${alias.domain ?: Contact.mainDomain}")
            if(aliasEmail == emailAddress){
                val aliasAccount = db.accountDao.getAccountById(alias.accountId)!!
                val accountEmail = aliasAccount.recipientId.plus("@${aliasAccount.domain}")
                return if(accountEmail == oldAccountAddress)
                    null
                else
                    aliasAccount
            }
        }
        return null
    }

    override fun cancel() {
        TODO("not implemented")
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { _ ->
        UIMessage(resId = R.string.error_updating_account)
    }
}