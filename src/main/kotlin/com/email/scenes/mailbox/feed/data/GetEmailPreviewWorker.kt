package com.email.scenes.mailbox.feed.data

import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.MailboxLocalDB
import com.email.db.models.Email
import com.email.db.models.Label
import com.email.email_preview.EmailPreview

/**
 * Created by danieltigse on 7/6/18.
 */

class GetEmailPreviewWorker(private val email: Email,
                            private val mailboxLocalDB: MailboxLocalDB,
                            private val userEmail: String,
                            override val publishFn: (FeedResult.GetEmailPreview) -> Unit)
    : BackgroundWorker<FeedResult.GetEmailPreview> {

    override fun catchException(ex: Exception): FeedResult.GetEmailPreview {
        val message = "Unexpected error: " + ex.message
        return FeedResult.GetEmailPreview.Failure(message)
    }

    override val canBeParallelized = true

    override fun work(reporter: ProgressReporter<FeedResult.GetEmailPreview>): FeedResult.GetEmailPreview {
        val emailThread = mailboxLocalDB.getEmailThreadFromEmail(
                email = email,
                userEmail = userEmail,
                selectedLabel = Label.defaultItems.inbox.text,
                rejectedLabels = listOf())
        return FeedResult.GetEmailPreview.Success(EmailPreview.fromEmailThread(emailThread))
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}