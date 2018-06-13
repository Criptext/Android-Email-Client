package com.email.scenes.emaildetail.workers

import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.EmailDetailLocalDB
import com.email.db.models.Label
import com.email.scenes.emaildetail.data.EmailDetailResult

/**
 * Created by sebas on 3/13/18.
 */

class LoadFullEmailsFromThreadWorker(
        private val db: EmailDetailLocalDB,
        private val threadId: String,
        private val currentLabel: Label,
        override val publishFn: (EmailDetailResult.LoadFullEmailsFromThreadId) -> Unit)
    : BackgroundWorker<EmailDetailResult.LoadFullEmailsFromThreadId> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): EmailDetailResult.LoadFullEmailsFromThreadId {
        return EmailDetailResult.LoadFullEmailsFromThreadId.Failure()
    }

    override fun work(reporter: ProgressReporter<EmailDetailResult.LoadFullEmailsFromThreadId>): EmailDetailResult.LoadFullEmailsFromThreadId {
        val rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(currentLabel).map { it.id }
        val items = db.getFullEmailsFromThreadId(threadId = threadId, rejectedLabels = rejectedLabels)
        return EmailDetailResult.LoadFullEmailsFromThreadId.Success(items)
    }

    override fun cancel() {
    }

}
