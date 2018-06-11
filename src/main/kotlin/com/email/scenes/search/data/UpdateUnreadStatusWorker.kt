package com.email.scenes.search.data

import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.MailboxLocalDB
import com.email.db.SearchLocalDB
import com.email.db.models.Label
import com.email.scenes.mailbox.data.EmailThread

/**
 * Created by danieltigse on 4/18/18.
 */

class UpdateUnreadStatusWorker(
        private val db: SearchLocalDB,
        private val emailThreads: List<EmailThread>,
        private val updateUnreadStatus: Boolean,
        private val currentLabel: Label,
        override val publishFn: (SearchResult.UpdateUnreadStatus) -> Unit)
    : BackgroundWorker<SearchResult.UpdateUnreadStatus> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): SearchResult.UpdateUnreadStatus {
        return SearchResult.UpdateUnreadStatus.Failure()
    }

    override fun work(reporter: ProgressReporter<SearchResult.UpdateUnreadStatus>): SearchResult.UpdateUnreadStatus? {
        val rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(currentLabel).map { it.id }
        db.updateUnreadStatus(emailThreads, updateUnreadStatus, rejectedLabels)
        return SearchResult.UpdateUnreadStatus.Success()
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

