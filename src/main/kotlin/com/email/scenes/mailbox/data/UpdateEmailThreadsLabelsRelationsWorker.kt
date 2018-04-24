package com.email.scenes.mailbox.data

import com.email.R
import com.email.bgworker.BackgroundWorker
import com.email.db.MailFolders
import com.email.db.MailboxLocalDB
import com.email.db.models.ActiveAccount
import com.email.scenes.labelChooser.SelectedLabels
import com.email.scenes.labelChooser.data.LabelWrapper
import com.email.utils.UIMessage

/**
 * Created by sebas on 04/05/18.
 */

class UpdateEmailThreadsLabelsRelationsWorker(
        private val db: MailboxLocalDB,
        private val activeAccount: ActiveAccount,
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

    override fun work(): MailboxResult.UpdateEmailThreadsLabelsRelations? {
        val emailIds = selectedEmailThreads.map {
            it.id
        }
        db.deleteRelationByEmailIds(emailIds = emailIds)
        val selectedLabels: SelectedLabels?
        if(chosenLabel == null) {
            selectedLabels = this.selectedLabels
        } else {
            selectedLabels = SelectedLabels()
                    selectedLabels.add(
                            LabelWrapper(db.getLabelFromLabelType(chosenLabel)))
        }
            emailIds.forEach{ emailId ->
                selectedLabels?.toIDs()?.forEach{labelId ->
                    db.createLabelEmailRelation(
                            emailId = emailId,
                            labelId = labelId)
                }
            }

        return MailboxResult.UpdateEmailThreadsLabelsRelations.Success()
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        UIMessage(resId = R.string.failed_getting_emails)
    }
}
