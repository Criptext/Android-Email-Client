package com.criptext.mail.scenes.emaildetail.workers

import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.EmailDetailLocalDB
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.utils.UIMessage

/**
 * Created by sebas on 3/20/18.
 */


class GetSelectedLabelsWorker(
        private val db: EmailDetailLocalDB,
        private val activeAccount: ActiveAccount,
        private val threadId: String,
        override val publishFn: (
                EmailDetailResult.GetSelectedLabels) -> Unit)
    : BackgroundWorker<EmailDetailResult.GetSelectedLabels> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): EmailDetailResult.GetSelectedLabels {
        return EmailDetailResult.GetSelectedLabels.Failure()
    }

    override fun work(reporter: ProgressReporter<EmailDetailResult.GetSelectedLabels>): EmailDetailResult.GetSelectedLabels? {
        val labels = db.getCustomLabels(activeAccount.id) as ArrayList<Label>
        labels.add(Label.defaultItems.starred)
        val defaultSelectedLabels = db.getLabelsFromThreadId(threadId)
        return EmailDetailResult.GetSelectedLabels.Success(
                allLabels = labels,
                selectedLabels = defaultSelectedLabels)
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { _ ->
            UIMessage(resId = R.string.failed_getting_labels)
    }
}
