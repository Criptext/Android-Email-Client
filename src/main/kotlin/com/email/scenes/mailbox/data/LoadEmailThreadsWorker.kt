package com.email.scenes.mailbox.data

import com.email.R
import com.email.bgworker.BackgroundWorker
import com.email.db.LabelTextTypes
import com.email.db.MailboxLocalDB
import com.email.db.models.ActiveAccount
import com.email.db.models.Label
import com.email.utils.UIMessage

/**
 * Created by sebas on 3/29/18.
 */

class LoadEmailThreadsWorker(
        private val db: MailboxLocalDB,
        private val activeAccount: ActiveAccount,
        private val offset: Int,
        private val labelTextTypes: LabelTextTypes,
        private val oldestEmailThread: EmailThread?,
        override val publishFn: (
                MailboxResult.LoadEmailThreads) -> Unit)
    : BackgroundWorker<MailboxResult.LoadEmailThreads> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.LoadEmailThreads {

        val message = createErrorMessage(ex)
        return MailboxResult.LoadEmailThreads.Failure(
                mailboxLabel = labelTextTypes,
                message = message,
                exception = ex)
    }

    override fun work(): MailboxResult.LoadEmailThreads? {
        val emailThreads = db.getEmailsFromMailboxLabel(
                labelTextType = labelTextTypes,
                oldestEmailThread = oldestEmailThread,
                rejectedLabels = selectRejectedLabels(),
                offset = offset)

        return MailboxResult.LoadEmailThreads.Success(
                emailThreads = emailThreads,
                mailboxLabel = labelTextTypes)
    }

    private fun selectRejectedLabels(): List<Label> {
        val commonRejectedLabels = listOf( LabelTextTypes.SPAM, LabelTextTypes.TRASH)
        return when(labelTextTypes) {
            LabelTextTypes.SENT,
            LabelTextTypes.INBOX,
            LabelTextTypes.ARCHIVED,
            LabelTextTypes.STARRED -> {
                db.getLabelsFromLabelType(
                        labelTextTypes = commonRejectedLabels)
            }
            LabelTextTypes.SPAM -> {
                db.getLabelsFromLabelType(
                        labelTextTypes = listOf(LabelTextTypes.TRASH))
            }
            else -> {
                emptyList()
            }
        }
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
                UIMessage(resId = R.string.failed_getting_emails)
    }
}
