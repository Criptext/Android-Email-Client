package com.criptext.mail.scenes.mailbox.workers

import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.mailbox.data.MailboxResult
import com.criptext.mail.utils.AccountUtils
import com.github.kittinunf.result.Result


class SetActiveAccountWorker(
        private val account: Account,
        private val db: MailboxLocalDB,
        private val storage: KeyValueStorage,
        override val publishFn: (MailboxResult.SetActiveAccount) -> Unit)
    : BackgroundWorker<MailboxResult.SetActiveAccount> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.SetActiveAccount {
        return MailboxResult.SetActiveAccount.Failure()
    }

    override fun work(reporter: ProgressReporter<MailboxResult.SetActiveAccount>)
            : MailboxResult.SetActiveAccount? {
        val operation =  Result.of {
            db.setActiveAccount(account.id)
            AccountUtils.setUserAsActiveAccount(account, storage)
        }
        return when(operation){
            is Result.Success -> MailboxResult.SetActiveAccount.Success(operation.value)
            is Result.Failure -> MailboxResult.SetActiveAccount.Failure()
        }
    }


    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}
