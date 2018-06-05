package com.email.scenes.emaildetail.workers

import com.email.R
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.EmailDetailLocalDB
import com.email.scenes.emaildetail.data.EmailDetailResult
import com.email.utils.UIMessage

/**
 * Created by sebas on 3/13/18.
 */


class LoadFullEmailsFromThreadWorker(
        private val db: EmailDetailLocalDB,
        private val threadId: String,
        override val publishFn: (EmailDetailResult.LoadFullEmailsFromThreadId) -> Unit)
    : BackgroundWorker<EmailDetailResult.LoadFullEmailsFromThreadId> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): EmailDetailResult.LoadFullEmailsFromThreadId {
        return EmailDetailResult.LoadFullEmailsFromThreadId.Failure()
    }

    override fun work(reporter: ProgressReporter<EmailDetailResult.LoadFullEmailsFromThreadId>)
        : EmailDetailResult.LoadFullEmailsFromThreadId {
        val items = db.getFullEmailsFromThreadId(threadId = threadId)
        return EmailDetailResult.LoadFullEmailsFromThreadId.Success(items)
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { _ ->
        UIMessage(resId = R.string.fail_register_try_again_error_exception)
    }
}
