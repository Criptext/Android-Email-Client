package com.email.scenes.emaildetail.workers

import com.email.R
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.EmailDetailLocalDB
import com.email.db.MailFolders
import com.email.db.models.EmailLabel
import com.email.db.models.Label
import com.email.scenes.emaildetail.data.EmailDetailResult
import com.email.scenes.label_chooser.SelectedLabels
import com.email.scenes.label_chooser.data.LabelWrapper
import com.email.utils.UIMessage

/**
 * Created by sebas on 04/05/18.
 */

class MoveEmailThreadWorker(
        private val db: EmailDetailLocalDB,
        private val chosenLabel: MailFolders?,
        private val threadId: String,
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

    override fun work(reporter: ProgressReporter<EmailDetailResult.MoveEmailThread>): EmailDetailResult.MoveEmailThread? {

        val rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(currentLabel).map { it.id }
        val emailIds = db.getFullEmailsFromThreadId(threadId, rejectedLabels).map {
            it.email.id
        }

        if(chosenLabel == null){
            //It means the threads will be deleted permanently
            db.deleteRelationByEmailIds(emailIds = emailIds)
            db.deleteThread(threadId)
            return EmailDetailResult.MoveEmailThread.Success(threadId)
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

        return EmailDetailResult.MoveEmailThread.Success(threadId)
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        UIMessage(resId = R.string.failed_getting_emails)
    }
}
