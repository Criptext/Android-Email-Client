package com.criptext.mail.scenes.mailbox.data

import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.utils.UIMessage

/**
 * Created by sebas on 3/20/18.
 */


class GetSelectedLabelsWorker(
        private val db: MailboxLocalDB,
        private val threadIds: List<String>,
        override val publishFn: (
                MailboxResult.GetSelectedLabels) -> Unit)
    : BackgroundWorker<MailboxResult.GetSelectedLabels> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.GetSelectedLabels {

        val message = createErrorMessage(ex)
        return MailboxResult.GetSelectedLabels.Failure(message, ex)
    }

    override fun work(reporter: ProgressReporter<MailboxResult.GetSelectedLabels>): MailboxResult.GetSelectedLabels? {
        val labels = db.getCustomLabels() as ArrayList<Label>
        labels.add(Label.defaultItems.starred)
        val defaultSelectedLabels = db.getLabelsFromThreadIds(
                threadIds = threadIds)

        return MailboxResult.GetSelectedLabels.Success(
                allLabels = labels,
                selectedLabels = defaultSelectedLabels)
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
            UIMessage(resId = R.string.failed_getting_labels)
    }
}
