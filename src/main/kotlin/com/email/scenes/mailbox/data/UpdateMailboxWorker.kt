package com.email.scenes.mailbox.data

import com.email.R
import com.email.bgworker.BackgroundWorker
import com.email.db.MailboxLocalDB
import com.email.utils.UIMessage

/**
 * Created by sebas on 3/22/18.
 */

class UpdateMailboxWorker(
        private val db: MailboxLocalDB,
        private val apiClient: MailboxAPIClient?,
        private val label: String,
        override val publishFn: (
                MailboxResult.UpdateMailbox) -> Unit)
    : BackgroundWorker<MailboxResult.UpdateMailbox> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.UpdateMailbox {

        val message = createErrorMessage(ex)
        return MailboxResult.UpdateMailbox.Failure(label, message)
    }

    override fun work(): MailboxResult.UpdateMailbox? {
        TODO("fetch emails from this account")
/*        return MailboxResult.GetLabels.Success(
                labels = labels,
                defaultSelectedLabels = defaultSelectedLabels)*/
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        UIMessage(resId = R.string.failed_getting_emails)
    }
}
