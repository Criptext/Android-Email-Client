package com.criptext.mail.utils.generaldatasource.workers

import android.content.res.Resources
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.push.data.IntentExtrasData
import com.criptext.mail.scenes.mailbox.data.MailboxResult
import com.criptext.mail.utils.AccountUtils
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result

class SetActiveAccountFromPushWorker(
        private val recipientId: String,
        private val domain: String,
        private val extras: IntentExtrasData,
        private val db: MailboxLocalDB,
        private val storage: KeyValueStorage,
        override val publishFn: (GeneralResult.SetActiveAccountFromPush) -> Unit)
    : BackgroundWorker<GeneralResult.SetActiveAccountFromPush> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): GeneralResult.SetActiveAccountFromPush {
        return GeneralResult.SetActiveAccountFromPush.Failure()
    }

    override fun work(reporter: ProgressReporter<GeneralResult.SetActiveAccountFromPush>)
            : GeneralResult.SetActiveAccountFromPush? {
        val operation =  Result.of {
            val account = db.getLoggedAccounts().find { it.recipientId == recipientId && it.domain == domain } ?: throw Resources.NotFoundException()
            db.setActiveAccount(account.id)
            AccountUtils.setUserAsActiveAccount(account, storage)
        }
        return when(operation){
            is Result.Success -> GeneralResult.SetActiveAccountFromPush.Success(operation.value, extras)
            is Result.Failure -> GeneralResult.SetActiveAccountFromPush.Failure()
        }
    }


    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}
