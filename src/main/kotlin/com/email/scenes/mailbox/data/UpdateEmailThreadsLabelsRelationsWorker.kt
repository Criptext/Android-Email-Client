package com.email.scenes.mailbox.data

import com.email.R
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.MailFolders
import com.email.db.MailboxLocalDB
import com.email.db.models.EmailLabel
import com.email.scenes.label_chooser.SelectedLabels
import com.email.scenes.label_chooser.data.LabelWrapper
import com.email.utils.UIMessage

/**
 * Created by sebas on 04/05/18.
 */

class UpdateEmailThreadsLabelsRelationsWorker(
        private val db: MailboxLocalDB,
        private val chosenLabel: MailFolders?,
        private val selectedLabels: SelectedLabels?,
        private val selectedEmailThreads: List<EmailThread>,
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

        val emailIds = selectedEmailThreads.map {
            it.id
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

        return MailboxResult.UpdateEmailThreadsLabelsRelations.Success()
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        UIMessage(resId = R.string.failed_getting_emails)
    }
}
