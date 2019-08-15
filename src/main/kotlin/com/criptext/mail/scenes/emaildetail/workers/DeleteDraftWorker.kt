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
import com.github.kittinunf.result.Result

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
        val operation = Result.of {
            db.deleteEmail(emailId, activeAccount)
        }
        return when(operation){
            is Result.Success -> {
                EmailDetailResult.DeleteDraft.Success(emailId)
            }
            is Result.Failure -> {
                catchException(operation.error)
            }
        }
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        UIMessage(resId = R.string.unable_to_delete_draft, args = arrayOf(ex.toString()))
    }
}
