package com.email.scenes.mailbox.data

import com.email.R
import com.email.bgworker.BackgroundWorker
import com.email.db.MailboxLocalDB
import com.email.db.models.ActiveAccount
import com.email.utils.UIMessage

/**
 * Created by sebas on 3/20/18.
 */


class GetSelectedLabelsWorker(
        private val db: MailboxLocalDB,
        private val activeAccount: ActiveAccount,
        private val threadIds: List<String>,
        override val publishFn: (
                MailboxResult.GetSelectedLabels) -> Unit)
    : BackgroundWorker<MailboxResult.GetSelectedLabels> {

    private val apiClient = MailboxAPIClient(activeAccount.jwt)
    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.GetSelectedLabels {

        val message = createErrorMessage(ex)
        return MailboxResult.GetSelectedLabels.Failure(message, ex)
    }

    override fun work(): MailboxResult.GetSelectedLabels? {
        val labels = db.getAllLabels()
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
