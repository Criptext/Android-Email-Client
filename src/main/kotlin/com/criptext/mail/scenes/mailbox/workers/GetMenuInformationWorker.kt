package com.criptext.mail.scenes.mailbox.workers

import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.mailbox.data.MailboxResult

/**
 * Created by danieltigse on 4/20/18.
 */

class GetMenuInformationWorker(
        private val db: MailboxLocalDB,
        private val activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        override val publishFn: (MailboxResult.GetMenuInformation) -> Unit)
    : BackgroundWorker<MailboxResult.GetMenuInformation> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): MailboxResult.GetMenuInformation {
        return MailboxResult.GetMenuInformation.Failure()
    }

    override fun work(reporter: ProgressReporter<MailboxResult.GetMenuInformation>)
            : MailboxResult.GetMenuInformation? {
        val account = db.getAccountByRecipientId(activeAccount.recipientId) ?: return MailboxResult.GetMenuInformation.Failure()

        val accounts = db.getLoggedAccounts()
        val jwts = accounts.map { it.jwt }.joinToString()
        storage.putString(KeyValueStorage.StringKey.JWTS, jwts)

        return MailboxResult.GetMenuInformation.Success(
                account = account,
                totalInbox = db.getUnreadCounterLabel(Label.defaultItems.inbox.id, account.id),
                totalSpam = db.getUnreadCounterLabel(Label.defaultItems.spam.id, account.id),
                totalDraft = db.getTotalCounterLabel(Label.defaultItems.draft.id, account.id),
                labels = db.getCustomAndVisibleLabels(account.id),
                accounts = accounts)
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}
