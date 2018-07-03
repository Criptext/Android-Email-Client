package com.email.scenes.mailbox.data

import com.email.R
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.MailboxLocalDB
import com.email.db.models.EmailLabel
import com.email.db.models.Label
import com.email.scenes.label_chooser.SelectedLabels
import com.email.utils.UIMessage

/**
 * Created by sebas on 04/05/18.
 */

class UpdateEmailThreadsLabelsWorker(
        private val db: MailboxLocalDB,
        private val selectedLabels: SelectedLabels,
        private val selectedThreadIds: List<String>,
        private val currentLabel: Label,
        private val shouldRemoveCurrentLabel: Boolean,
        override val publishFn: (
                MailboxResult.UpdateEmailThreadsLabelsRelations) -> Unit)
    : BackgroundWorker<MailboxResult.UpdateEmailThreadsLabelsRelations> {

    private val defaultItems = Label.DefaultItems()
    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.UpdateEmailThreadsLabelsRelations {

        val message = createErrorMessage(ex)
        return MailboxResult.UpdateEmailThreadsLabelsRelations.Failure(
                message = message,
                exception = ex)
    }

    private fun removeCurrentLabelFromEmails(emailIds: List<Long>) {
        if(currentLabel == defaultItems.starred
          || currentLabel == defaultItems.important
          || currentLabel == defaultItems.sent)
            db.deleteRelationByLabelAndEmailIds(Label.defaultItems.inbox.id, emailIds)
        else
            db.deleteRelationByLabelAndEmailIds(currentLabel.id, emailIds)
    }

    private fun updateLabelEmailRelations(emailIds: List<Long>) {
        db.deleteRelationByEmailIds(emailIds = emailIds)

        val emailLabels =
                emailIds.flatMap { emailId ->
                    selectedLabels.toIDs().map { labelId ->
                        EmailLabel(emailId = emailId, labelId = labelId)
                    }
                }
        db.createLabelEmailRelations(emailLabels)
    }

    override fun work(reporter: ProgressReporter<MailboxResult.UpdateEmailThreadsLabelsRelations>)
            : MailboxResult.UpdateEmailThreadsLabelsRelations? {

        val rejectedLabels = defaultItems.rejectedLabelsByMailbox(currentLabel).map { it.id }
        val emailIds = selectedThreadIds.flatMap { threadId ->
            db.getEmailsByThreadId(threadId, rejectedLabels).map { it.id }
        }

        if(shouldRemoveCurrentLabel)
            removeCurrentLabelFromEmails(emailIds)
        else
            updateLabelEmailRelations(emailIds)

        return MailboxResult.UpdateEmailThreadsLabelsRelations.Success()
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        UIMessage(resId = R.string.failed_getting_emails)
    }
}
