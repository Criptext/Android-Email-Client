package com.email.scenes.emaildetail.workers

import com.email.R
import com.email.bgworker.BackgroundWorker
import com.email.db.EmailDetailLocalDB
import com.email.db.MailFolders
import com.email.db.MailboxLocalDB
import com.email.db.models.ActiveAccount
import com.email.db.models.EmailLabel
import com.email.db.models.FullEmail
import com.email.scenes.emaildetail.data.EmailDetailResult
import com.email.scenes.labelChooser.SelectedLabels
import com.email.scenes.labelChooser.data.LabelWrapper
import com.email.utils.UIMessage

/**
 * Created by danieltigse on 05/01/18.
 */

class UpdateEmailLabelsRelationsWorker(
        private val db: EmailDetailLocalDB,
        private val chosenLabel: MailFolders?,
        private val selectedLabels: SelectedLabels?,
        private val threadId: String,
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

    override fun work(): EmailDetailResult.UpdateEmailThreadsLabelsRelations? {

        val emailIds = db.getFullEmailsFromThreadId(threadId).map {
            it.email.id
        }

        db.deleteRelationByEmailIds(emailIds = emailIds)
        var selectedLabels = SelectedLabels()
        if(this.selectedLabels != null) {
            selectedLabels = this.selectedLabels
        }
        else if(chosenLabel != null){
            selectedLabels.add(LabelWrapper(db.getLabelFromLabelType(chosenLabel)))
        }

        val emailLabels = arrayListOf<EmailLabel>()
        emailIds.flatMap{ emailId ->
            selectedLabels.toIDs().map{ labelId ->
                emailLabels.add(EmailLabel(
                        emailId = emailId,
                        labelId = labelId))
            }
        }
        db.createLabelEmailRelations(emailLabels)

        return EmailDetailResult.UpdateEmailThreadsLabelsRelations.Success()
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        UIMessage(resId = R.string.failed_getting_emails)
    }
}
