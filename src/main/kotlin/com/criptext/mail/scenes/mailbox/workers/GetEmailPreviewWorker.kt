package com.criptext.mail.scenes.mailbox.workers

import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.mailbox.data.MailboxResult
import com.criptext.mail.utils.EmailThreadValidator
import com.github.kittinunf.result.Result


/**
 * Created by danieltigse on 7/6/18.
 */

class GetEmailPreviewWorker(private val threadId:String,
                            private val mailboxLocalDB: MailboxLocalDB,
                            private val doReply: Boolean = false,
                            private val userEmail: String,
                            override val publishFn: (MailboxResult.GetEmailPreview) -> Unit)
    : BackgroundWorker<MailboxResult.GetEmailPreview> {

    override fun catchException(ex: Exception): MailboxResult.GetEmailPreview {
        val message = "Unexpected error: " + ex.message
        return MailboxResult.GetEmailPreview.Failure(message)
    }

    override val canBeParallelized = true

    override fun work(reporter: ProgressReporter<MailboxResult.GetEmailPreview>): MailboxResult.GetEmailPreview {
        val emailThreadResult = Result.of {mailboxLocalDB.getEmailThreadFromId(
                threadId = threadId,
                userEmail = userEmail,
                selectedLabel = Label.defaultItems.inbox.text,
                rejectedLabels = listOf())}
        return when(emailThreadResult) {
            is Result.Success -> {
                val labels = mailboxLocalDB.getLabelsFromThreadIds(listOf(emailThreadResult.value.threadId))
                MailboxResult.GetEmailPreview.Success(
                        emailPreview = EmailPreview.fromEmailThread(emailThreadResult.value),
                        isTrash = EmailThreadValidator.isLabelInList(labels, Label.LABEL_TRASH),
                        isSpam = EmailThreadValidator.isLabelInList(labels, Label.LABEL_SPAM),
                        doReply = doReply)
            }
            is Result.Failure -> {
                catchException(emailThreadResult.error)
            }
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}