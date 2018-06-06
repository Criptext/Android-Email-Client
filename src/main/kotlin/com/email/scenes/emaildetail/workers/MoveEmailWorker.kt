package com.email.scenes.emaildetail.workers

import com.email.R
import com.email.bgworker.BackgroundWorker
import com.email.db.EmailDetailLocalDB
import com.email.db.MailFolders
import com.email.db.MailboxLocalDB
import com.email.db.models.ActiveAccount
import com.email.db.models.EmailLabel
import com.email.db.models.Label
import com.email.scenes.emaildetail.data.EmailDetailResult
import com.email.scenes.labelChooser.SelectedLabels
import com.email.scenes.labelChooser.data.LabelWrapper
import com.email.scenes.mailbox.data.EmailThread
import com.email.utils.UIMessage

/**
 * Created by danieltigse on 5/6/18.
 */

class MoveEmailWorker(
        private val db: EmailDetailLocalDB,
        private val chosenLabel: MailFolders?,
        private val emailId: Long,
        private val currentLabel: Label,
        override val publishFn: (
                EmailDetailResult.MoveEmailThread) -> Unit)
    : BackgroundWorker<EmailDetailResult.MoveEmailThread> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): EmailDetailResult.MoveEmailThread {

        val message = createErrorMessage(ex)
        return EmailDetailResult.MoveEmailThread.Failure(
                message = message,
                exception = ex)
    }

    override fun work(): EmailDetailResult.MoveEmailThread? {

        val emailIds = listOf(emailId)

        if(chosenLabel == null){
            //It means the threads will be deleted permanently
            db.deleteRelationByEmailIds(emailIds = emailIds)
            db.deleteEmail(emailId)
            return EmailDetailResult.MoveEmailThread.Success()
        }

        if(currentLabel == Label.defaultItems.trash && chosenLabel == MailFolders.SPAM){
            //Mark as spam from trash
            db.deleteRelationByLabelAndEmailIds(labelId = Label.defaultItems.trash.id,
                                                emailIds = emailIds)
        }

        val selectedLabels = SelectedLabels()
        selectedLabels.add(LabelWrapper(db.getLabelFromLabelType(chosenLabel)))

        val emailLabels = arrayListOf<EmailLabel>()
        emailIds.flatMap{ emailId ->
            selectedLabels.toIDs().map{ labelId ->
                emailLabels.add(EmailLabel(
                        emailId = emailId,
                        labelId = labelId))
            }
        }
        db.createLabelEmailRelations(emailLabels)

        return EmailDetailResult.MoveEmailThread.Success()
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        UIMessage(resId = R.string.failed_getting_emails)
    }
}
