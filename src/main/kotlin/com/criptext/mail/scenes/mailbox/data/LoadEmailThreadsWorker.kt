package com.criptext.mail.scenes.mailbox.data

import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.utils.UIMessage

/**
 * Created by sebas on 3/29/18.
 */

class LoadEmailThreadsWorker(
        private val db: MailboxLocalDB,
        private val loadParams: LoadParams,
        private val labelNames: String,
        private val userEmail: String,
        override val publishFn: (
                MailboxResult.LoadEmailThreads) -> Unit)
    : BackgroundWorker<MailboxResult.LoadEmailThreads> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): MailboxResult.LoadEmailThreads {

        val message = createErrorMessage(ex)
        return MailboxResult.LoadEmailThreads.Failure(
                mailboxLabel = labelNames,
                message = message,
                exception = ex)
    }

    private fun loadThreadsWithParams(): List<EmailThread> = when (loadParams) {
        is LoadParams.NewPage -> db.getThreadsFromMailboxLabel(
            labelName = labelNames,
            startDate = loadParams.startDate,
            rejectedLabels = Label.defaultItems.rejectedLabelsByFolder(labelNames),
            limit = loadParams.size,
            userEmail = userEmail)
        is LoadParams.Reset -> db.getThreadsFromMailboxLabel(
            labelName = labelNames,
            startDate = null,
            rejectedLabels = Label.defaultItems.rejectedLabelsByFolder(labelNames),
            limit = loadParams.size,
            userEmail = userEmail)
    }

    override fun work(reporter: ProgressReporter<MailboxResult.LoadEmailThreads>)
            : MailboxResult.LoadEmailThreads? {
        val emailThreads = loadThreadsWithParams()
        val emailPreviews = emailThreads.map { EmailPreview.fromEmailThread(it) }

        return MailboxResult.LoadEmailThreads.Success(
                emailPreviews = emailPreviews,
                mailboxLabel = labelNames,
                isReset = loadParams is LoadParams.Reset)
    }


    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { _ ->
                UIMessage(resId = R.string.failed_getting_emails)
    }
}
