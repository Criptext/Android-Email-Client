package com.email.scenes.mailbox.data

import com.email.R
import com.email.bgworker.BackgroundWorker
import com.email.db.MailFolders
import com.email.db.MailboxLocalDB
import com.email.db.models.ActiveAccount
import com.email.db.models.EmailLabel
import com.email.db.models.Label
import com.email.scenes.labelChooser.SelectedLabels
import com.email.scenes.labelChooser.data.LabelWrapper
import com.email.utils.UIMessage

/**
 * Created by sebas on 04/05/18.
 */

class MoveEmailThreadWorker(
        private val db: MailboxLocalDB,
        private val chosenLabel: MailFolders?,
        private val selectedEmailThreads: List<EmailThread>,
        private val currentLabel: Label,
        override val publishFn: (
                MailboxResult.MoveEmailThread) -> Unit)
    : BackgroundWorker<MailboxResult.MoveEmailThread> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.MoveEmailThread {

        val message = createErrorMessage(ex)
        return MailboxResult.MoveEmailThread.Failure(
                message = message,
                exception = ex)
    }

    override fun work(): MailboxResult.MoveEmailThread? {

        val rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(currentLabel).map { it.id }
        val emailIds = selectedEmailThreads.flatMap {
            db.getEmailsByThreadId(it.threadId, rejectedLabels).map {
                it.id
            }
        }

        if(chosenLabel == null){
            //It means the threads will be deleted permanently
            db.deleteRelationByEmailIds(emailIds = emailIds)
            db.deleteThreads(threadIds = selectedEmailThreads.map { it.threadId })
            return MailboxResult.MoveEmailThread.Success()
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

        return MailboxResult.MoveEmailThread.Success()
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        UIMessage(resId = R.string.failed_getting_emails)
    }
}
