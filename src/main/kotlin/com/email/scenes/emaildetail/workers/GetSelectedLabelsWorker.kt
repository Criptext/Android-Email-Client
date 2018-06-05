package com.email.scenes.emaildetail.workers

import com.email.R
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.EmailDetailLocalDB
import com.email.db.MailboxLocalDB
import com.email.db.models.ActiveAccount
import com.email.db.models.Label
import com.email.scenes.emaildetail.data.EmailDetailResult
import com.email.utils.UIMessage

/**
 * Created by sebas on 3/20/18.
 */


class GetSelectedLabelsWorker(
        private val db: EmailDetailLocalDB,
        private val threadId: String,
        override val publishFn: (
                EmailDetailResult.GetSelectedLabels) -> Unit)
    : BackgroundWorker<EmailDetailResult.GetSelectedLabels> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): EmailDetailResult.GetSelectedLabels {
        return EmailDetailResult.GetSelectedLabels.Failure()
    }

    override fun work(reporter: ProgressReporter<EmailDetailResult.GetSelectedLabels>)
            : EmailDetailResult.GetSelectedLabels? {
        val labels = Label.defaultItems.toList()
        val defaultSelectedLabels = db.getLabelsFromThreadId(
                threadId = threadId)
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
