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
        private val selectedEmailThreads: List<EmailThread>,
        private val currentLabel: Label,
        private val removeCurrentLabel: Boolean,
        override val publishFn: (
                MailboxResult.UpdateEmailThreadsLabelsRelations) -> Unit)
    : BackgroundWorker<MailboxResult.UpdateEmailThreadsLabelsRelations> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.UpdateEmailThreadsLabelsRelations {

        val message = createErrorMessage(ex)
        return MailboxResult.UpdateEmailThreadsLabelsRelations.Failure(
                message = message,
                exception = ex)
    }

    override fun work(reporter: ProgressReporter<MailboxResult.UpdateEmailThreadsLabelsRelations>)
            : MailboxResult.UpdateEmailThreadsLabelsRelations? {

        val rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(currentLabel).map { it.id }
        val emailIds = selectedEmailThreads.flatMap {
            db.getEmailsByThreadId(it.threadId, rejectedLabels).map {
                it.id
            }
        }

        if(removeCurrentLabel){
            if(currentLabel == Label.defaultItems.starred
                    || currentLabel == Label.defaultItems.important
                    || currentLabel == Label.defaultItems.sent){
                db.deleteRelationByLabelAndEmailIds(Label.defaultItems.inbox.id, emailIds)
            }
            else{
                db.deleteRelationByLabelAndEmailIds(currentLabel.id, emailIds)
            }
        }
        else{
            db.deleteRelationByEmailIds(emailIds = emailIds)

            val emailLabels = arrayListOf<EmailLabel>()
            emailIds.flatMap{ emailId ->
                selectedLabels.toIDs().map{ labelId ->
                    emailLabels.add(EmailLabel(
                            emailId = emailId,
                            labelId = labelId))
                }
            }
            db.createLabelEmailRelations(emailLabels)
        }

        return MailboxResult.UpdateEmailThreadsLabelsRelations.Success()
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        UIMessage(resId = R.string.failed_getting_emails)
    }
}
