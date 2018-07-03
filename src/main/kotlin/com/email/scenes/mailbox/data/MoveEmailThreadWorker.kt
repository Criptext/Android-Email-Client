package com.email.scenes.mailbox.data

import com.email.R
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.MailFolders
import com.email.db.MailboxLocalDB
import com.email.db.models.EmailLabel
import com.email.db.models.Label
import com.email.scenes.label_chooser.SelectedLabels
import com.email.scenes.label_chooser.data.LabelWrapper
import com.email.utils.UIMessage

/**
 * Created by sebas on 04/05/18.
 */

class MoveEmailThreadWorker(
        private val db: MailboxLocalDB,
        private val chosenLabel: MailFolders?,
        private val selectedThreadIds: List<String>,
        private val currentLabel: Label,
        override val publishFn: (
                MailboxResult.MoveEmailThread) -> Unit)
    : BackgroundWorker<MailboxResult.MoveEmailThread> {

    private val defaultItems = Label.DefaultItems()
    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.MoveEmailThread {

        val message = createErrorMessage(ex)
        return MailboxResult.MoveEmailThread.Failure(
                message = message,
                exception = ex)
    }

    private fun getLabelEmailRelationsFromEmailIds(emailIds: List<Long>, label: MailFolders): List<EmailLabel> {
        val selectedLabels = SelectedLabels()
        selectedLabels.add(LabelWrapper(db.getLabelFromLabelType(label)))


        return emailIds.flatMap{ emailId ->
                selectedLabels.toIDs().map{ labelId ->
                    EmailLabel(emailId = emailId, labelId = labelId)
                }
            }
    }

    override fun work(reporter: ProgressReporter<MailboxResult.MoveEmailThread>)
            : MailboxResult.MoveEmailThread? {

        if(chosenLabel == null){
            //It means the threads will be deleted permanently
            db.deleteThreads(threadIds = selectedThreadIds)
            return MailboxResult.MoveEmailThread.Success()
        }

        val rejectedLabels = defaultItems.rejectedLabelsByMailbox(currentLabel).map { it.id }
        val emailIds = selectedThreadIds.flatMap { threadId ->
            db.getEmailsByThreadId(threadId, rejectedLabels).map { it.id }
        }

        if(currentLabel == Label.defaultItems.trash && chosenLabel == MailFolders.SPAM){
            //Mark as spam from trash
            db.deleteRelationByLabelAndEmailIds(labelId = defaultItems.trash.id,
                                                emailIds = emailIds)
        }

        val labelEmails = getLabelEmailRelationsFromEmailIds(emailIds, chosenLabel)
        db.createLabelEmailRelations(labelEmails)

        return MailboxResult.MoveEmailThread.Success()
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        UIMessage(resId = R.string.failed_getting_emails)
    }
}
