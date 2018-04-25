package com.email.scenes.mailbox.data

import com.email.bgworker.BackgroundWorker
import com.email.db.MailFolders
import com.email.db.MailboxLocalDB
import com.email.db.models.Label

/**
 * Created by danieltigse on 4/20/18.
 */

class GetMenuInformationWorker(
        private val db: MailboxLocalDB,
        override val publishFn: (MailboxResult.GetMenuInformation) -> Unit)
    : BackgroundWorker<MailboxResult.GetMenuInformation> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): MailboxResult.GetMenuInformation {
        return MailboxResult.GetMenuInformation.Failure()
    }

    override fun work(): MailboxResult.GetMenuInformation? {
        return MailboxResult.GetMenuInformation.Success(
                account = db.getExistingAccount(),
                totalInbox = db.getUnreadCounterLabel(Label.defaultItems.inbox.id),
                totalSpam = db.getUnreadCounterLabel(Label.defaultItems.spam.id),
                totalDraft = db.getTotalCounterLabel(Label.defaultItems.draft.id))
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
