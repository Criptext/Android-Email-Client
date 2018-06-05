package com.email.scenes.emaildetail.workers

import com.email.R
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.EmailDetailLocalDB
import com.email.scenes.emaildetail.data.EmailDetailResult
import com.email.utils.UIMessage

/**
 * Created by sebas on 3/22/18.
 */


class UnsendFullEmailWorker(
        private val db: EmailDetailLocalDB,
        private val emailId: Long,
        private val position: Int,
        override val publishFn: (EmailDetailResult.UnsendFullEmailFromEmailId) -> Unit)
    : BackgroundWorker<EmailDetailResult.UnsendFullEmailFromEmailId> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception):
            EmailDetailResult.UnsendFullEmailFromEmailId {

        val message = createErrorMessage(ex)
        return EmailDetailResult.UnsendFullEmailFromEmailId.
                Failure(position, message, ex)
    }

    override fun work(reporter: ProgressReporter<EmailDetailResult.UnsendFullEmailFromEmailId>)
            : EmailDetailResult.UnsendFullEmailFromEmailId {
        Thread.sleep(500)
        db.unsendEmail(emailId)
        return EmailDetailResult.UnsendFullEmailFromEmailId.Success(position)
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { _ ->
        UIMessage(resId = R.string.fail_unsend_email)
    }
}
