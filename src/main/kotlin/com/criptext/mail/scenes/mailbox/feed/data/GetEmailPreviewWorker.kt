package com.criptext.mail.scenes.mailbox.feed.data

import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Email
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.utils.EmailThreadValidator

/**
 * Created by danieltigse on 7/6/18.
 */

class GetEmailPreviewWorker(private val email: Email,
                            private val activeAccount: ActiveAccount,
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
                rejectedLabels = listOf(),
                activeAccount = activeAccount)
        val labels = mailboxLocalDB.getLabelsFromThreadIds(listOf(email.threadId))
        return FeedResult.GetEmailPreview.Success(
                emailPreview = EmailPreview.fromEmailThread(emailThread),
                isTrash = EmailThreadValidator.isLabelInList(labels, Label.LABEL_TRASH),
                isSpam = EmailThreadValidator.isLabelInList(labels, Label.LABEL_SPAM))
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}