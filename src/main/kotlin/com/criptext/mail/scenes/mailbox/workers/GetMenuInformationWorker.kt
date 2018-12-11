package com.criptext.mail.scenes.mailbox.workers

import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.mailbox.data.MailboxResult

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

    override fun work(reporter: ProgressReporter<MailboxResult.GetMenuInformation>)
            : MailboxResult.GetMenuInformation? {
        return MailboxResult.GetMenuInformation.Success(
                account = db.getExistingAccount(),
                totalInbox = db.getUnreadCounterLabel(Label.defaultItems.inbox.id),
                totalSpam = db.getUnreadCounterLabel(Label.defaultItems.spam.id),
                totalDraft = db.getTotalCounterLabel(Label.defaultItems.draft.id),
                labels = db.getCustomAndVisibleLabels())
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}
