package com.criptext.mail.scenes.emaildetail.workers

import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.ComposerLocalDB
import com.criptext.mail.db.EmailDetailLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.composer.data.ComposerResult
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.utils.UIMessage

class DeleteDraftWorker(
        private val db: EmailDetailLocalDB,
        private val emailId: Long,
        private val activeAccount: ActiveAccount,
        override val publishFn: (
                EmailDetailResult.DeleteDraft) -> Unit)
    : BackgroundWorker<EmailDetailResult.DeleteDraft> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): EmailDetailResult.DeleteDraft {
        val message = createErrorMessage(ex)
        return EmailDetailResult.DeleteDraft.Failure(
                message = message,
                exception = ex)
    }

    override fun work(reporter: ProgressReporter<EmailDetailResult.DeleteDraft>): EmailDetailResult.DeleteDraft? {
        db.deleteEmail(emailId, activeAccount)
        return EmailDetailResult.DeleteDraft.Success(emailId)
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { _ ->
        UIMessage(resId = R.string.failed_getting_emails)
    }
}
