package com.criptext.mail.scenes.emaildetail.workers

import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.EmailDetailLocalDB
import com.criptext.mail.db.models.EmailLabel
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.scenes.label_chooser.SelectedLabels
import com.criptext.mail.utils.UIMessage

/**
 * Created by danieltigse on 05/01/18.
 */

class UpdateEmailThreadLabelsWorker(
        private val db: EmailDetailLocalDB,
        private val currentLabel: Label,
        private val selectedLabels: SelectedLabels,
        private val threadId: String,
        private val removeCurrentLabel: Boolean,
        override val publishFn: (
                EmailDetailResult.UpdateEmailThreadsLabelsRelations) -> Unit)
    : BackgroundWorker<EmailDetailResult.UpdateEmailThreadsLabelsRelations> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): EmailDetailResult.UpdateEmailThreadsLabelsRelations {

        val message = createErrorMessage(ex)
        return EmailDetailResult.UpdateEmailThreadsLabelsRelations.Failure(
                message = message,
                exception = ex)
    }

    override fun work(reporter: ProgressReporter<EmailDetailResult.UpdateEmailThreadsLabelsRelations>)
            : EmailDetailResult.UpdateEmailThreadsLabelsRelations? {

        val rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(currentLabel).map { it.id }
        val emailIds = db.getFullEmailsFromThreadId(threadId = threadId, rejectedLabels = rejectedLabels).map {
            it.email.id
        }

        if(removeCurrentLabel){
            if(currentLabel == Label.defaultItems.starred
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

        val labels = db.getLabelsFromThreadId(threadId)

        return EmailDetailResult.UpdateEmailThreadsLabelsRelations.Success(threadId, labels)
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { _ ->
        UIMessage(resId = R.string.failed_getting_emails)
    }
}
