package com.email.scenes.mailbox.data

import com.email.R
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.MailFolders
import com.email.db.MailboxLocalDB
import com.email.db.models.Label
import com.email.email_preview.EmailPreview
import com.email.utils.UIMessage

/**
 * Created by sebas on 3/29/18.
 */

class LoadEmailThreadsWorker(
        private val db: MailboxLocalDB,
        private val loadParams: LoadParams,
        private val labelTextTypes: MailFolders,
        private val userEmail: String,
        override val publishFn: (
                MailboxResult.LoadEmailThreads) -> Unit)
    : BackgroundWorker<MailboxResult.LoadEmailThreads> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): MailboxResult.LoadEmailThreads {

        val message = createErrorMessage(ex)
        return MailboxResult.LoadEmailThreads.Failure(
                mailboxLabel = labelTextTypes,
                message = message,
                exception = ex)
    }

    private fun loadThreadsWithParams(): List<EmailThread> = when (loadParams) {
        is LoadParams.NewPage -> db.getThreadsFromMailboxLabel(
            labelTextTypes = labelTextTypes,
            startDate = loadParams.startDate,
            rejectedLabels = Label.defaultItems.rejectedLabelsByFolder(labelTextTypes),
            limit = loadParams.size,
            userEmail = userEmail)
        is LoadParams.Reset -> db.getThreadsFromMailboxLabel(
            labelTextTypes = labelTextTypes,
            startDate = null,
            rejectedLabels = Label.defaultItems.rejectedLabelsByFolder(labelTextTypes),
            limit = loadParams.size,
            userEmail = userEmail)
    }

    override fun work(reporter: ProgressReporter<MailboxResult.LoadEmailThreads>)
            : MailboxResult.LoadEmailThreads? {
        val emailThreads = loadThreadsWithParams()
        val emailPreviews = emailThreads.map { EmailPreview.fromEmailThread(it) }

        return MailboxResult.LoadEmailThreads.Success(
                emailPreviews = emailPreviews,
                mailboxLabel = labelTextTypes,
                isReset = loadParams is LoadParams.Reset)
    }


    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { _ ->
                UIMessage(resId = R.string.failed_getting_emails)
    }
}
