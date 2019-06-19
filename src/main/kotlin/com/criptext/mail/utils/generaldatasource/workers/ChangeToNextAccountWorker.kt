package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.mailbox.data.MailboxResult
import com.criptext.mail.utils.AccountUtils
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result


class ChangeToNextAccountWorker(
        private val db: MailboxLocalDB,
        private val storage: KeyValueStorage,
        override val publishFn: (GeneralResult.ChangeToNextAccount) -> Unit)
    : BackgroundWorker<GeneralResult.ChangeToNextAccount> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): GeneralResult.ChangeToNextAccount {
        return GeneralResult.ChangeToNextAccount.Failure()
    }

    override fun work(reporter: ProgressReporter<GeneralResult.ChangeToNextAccount>)
            : GeneralResult.ChangeToNextAccount? {
        val operation =  Result.of {
            val accounts = db.getLoggedAccounts()
            val currentActiveAccount = accounts.find { it.isActive }!!
            val newActiveAccount = getNextAccount(currentActiveAccount, accounts)
            db.setActiveAccount(newActiveAccount.id)
            AccountUtils.setUserAsActiveAccount(newActiveAccount, storage)
        }
        return when(operation){
            is Result.Success -> GeneralResult.ChangeToNextAccount.Success(operation.value)
            is Result.Failure -> GeneralResult.ChangeToNextAccount.Failure()
        }
    }

    private fun getNextAccount(activeAccount: Account, accounts: List<Account>): Account{
        val index = accounts.indexOf(activeAccount)
        if(index > -1){
            return if(index == accounts.lastIndex)
                accounts.first()
            else
                accounts[index + 1]
        }
        return activeAccount
    }


    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}
