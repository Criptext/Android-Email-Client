package com.email.scenes.mailbox.data

import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.MailboxLocalDB
import com.email.db.models.Label

/**
 * Created by danieltigse on 4/18/18.
 */

class UpdateUnreadStatusWorker(
        private val db: MailboxLocalDB,
        private val emailThreads: List<EmailThread>,
        private val updateUnreadStatus: Boolean,
        private val currentLabel: Label,
        override val publishFn: (MailboxResult.UpdateUnreadStatus) -> Unit)
    : BackgroundWorker<MailboxResult.UpdateUnreadStatus> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.UpdateUnreadStatus {
        return MailboxResult.UpdateUnreadStatus.Failure()
    }

    override fun work(reporter: ProgressReporter<MailboxResult.UpdateUnreadStatus>): MailboxResult.UpdateUnreadStatus? {
        val rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(currentLabel).map { it.id }
        db.updateUnreadStatus(emailThreads, updateUnreadStatus, rejectedLabels)
        return MailboxResult.UpdateUnreadStatus.Success()
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}

