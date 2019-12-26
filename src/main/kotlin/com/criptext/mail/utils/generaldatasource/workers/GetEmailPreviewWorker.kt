package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.mailbox.data.MailboxResult
import com.criptext.mail.utils.EmailThreadValidator
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result
import kotlinx.android.synthetic.main.restore_backup_dialog.view.*


/**
 * Created by danieltigse on 7/6/18.
 */

class GetEmailPreviewWorker(private val threadId:String,
                            private val activeAccount: ActiveAccount,
                            private val activityMessage: ActivityMessage?,
                            private val mailboxLocalDB: MailboxLocalDB,
                            private val doReply: Boolean = false,
                            private val userEmail: String,
                            override val publishFn: (GeneralResult.GetEmailPreview) -> Unit)
    : BackgroundWorker<GeneralResult.GetEmailPreview> {

    override fun catchException(ex: Exception): GeneralResult.GetEmailPreview {
        return GeneralResult.GetEmailPreview.Failure(UIMessage(R.string.local_error, arrayOf(ex.toString())))
    }

    override val canBeParallelized = true

    override fun work(reporter: ProgressReporter<GeneralResult.GetEmailPreview>): GeneralResult.GetEmailPreview {
        val emailThreadResult = Result.of {mailboxLocalDB.getEmailThreadFromId(
                threadId = threadId,
                userEmail = userEmail,
                selectedLabel = Label.defaultItems.inbox.text,
                activeAccount = activeAccount,
                rejectedLabels = listOf())}
        return when(emailThreadResult) {
            is Result.Success -> {
                val labels = mailboxLocalDB.getLabelsFromThreadIds(listOf(emailThreadResult.value.threadId))
                GeneralResult.GetEmailPreview.Success(
                        emailPreview = EmailPreview.fromEmailThread(emailThreadResult.value),
                        isTrash = EmailThreadValidator.isLabelInList(labels, Label.LABEL_TRASH),
                        isSpam = EmailThreadValidator.isLabelInList(labels, Label.LABEL_SPAM),
                        doReply = doReply, activityMessage = activityMessage)
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