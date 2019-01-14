package com.criptext.mail.scenes.mailbox.workers

import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.mailbox.data.EmailThread
import com.criptext.mail.scenes.mailbox.data.LoadParams
import com.criptext.mail.scenes.mailbox.data.MailboxResult
import com.criptext.mail.utils.UIMessage

/**
 * Created by sebas on 3/29/18.
 */

class LoadEmailThreadsWorker(
        private val db: MailboxLocalDB,
        private val loadParams: LoadParams,
        private val filterUnread: Boolean,
        private val labelNames: String,
        private val userEmail: String,
        override val publishFn: (
                MailboxResult.LoadEmailThreads) -> Unit)
    : BackgroundWorker<MailboxResult.LoadEmailThreads> {

    override val canBeParallelized = false

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
            userEmail = userEmail,
            filterUnread = filterUnread)
        is LoadParams.Reset -> db.getThreadsFromMailboxLabel(
            labelName = labelNames,
            startDate = null,
            rejectedLabels = Label.defaultItems.rejectedLabelsByFolder(labelNames),
            limit = loadParams.size,
            userEmail = userEmail,
            filterUnread = filterUnread)
        is LoadParams.UpdatePage -> {
            val newEmails = db.getNewThreadsFromMailboxLabel(
                    labelName = labelNames,
                    mostRecentDate = loadParams.mostRecentDate,
                    rejectedLabels = Label.defaultItems.rejectedLabelsByFolder(labelNames),
                    userEmail = userEmail
            )
            db.getThreadsFromMailboxLabel(
                    labelName = labelNames,
                    startDate = null,
                    rejectedLabels = Label.defaultItems.rejectedLabelsByFolder(labelNames),
                    limit = loadParams.size + newEmails.size,
                    userEmail = userEmail,
                    filterUnread = filterUnread)
        }
    }

    override fun work(reporter: ProgressReporter<MailboxResult.LoadEmailThreads>)
            : MailboxResult.LoadEmailThreads? {
        val emailThreads = loadThreadsWithParams()
        val emailPreviews = emailThreads.map { EmailPreview.fromEmailThread(it) }

        return MailboxResult.LoadEmailThreads.Success(
                emailPreviews = emailPreviews,
                mailboxLabel = labelNames,
                loadParams = if (loadParams is LoadParams.UpdatePage)
                    loadParams.copy(size = emailPreviews.size)
                else loadParams)
    }


    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { _ ->
                UIMessage(resId = R.string.failed_getting_emails)
    }
}
