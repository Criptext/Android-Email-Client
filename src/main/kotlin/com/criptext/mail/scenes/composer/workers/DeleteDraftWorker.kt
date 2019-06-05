package com.criptext.mail.scenes.composer.workers

import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.ComposerLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.composer.data.ComposerResult
import com.criptext.mail.utils.UIMessage

/**
 * Created by danieltigse on 5/6/18.
 */

class DeleteDraftWorker(
        private val db: ComposerLocalDB,
        private val emailId: Long,
        private val activeAccount: ActiveAccount,
        override val publishFn: (
                ComposerResult.DeleteDraft) -> Unit)
    : BackgroundWorker<ComposerResult.DeleteDraft> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): ComposerResult.DeleteDraft {
        val message = createErrorMessage(ex)
        return ComposerResult.DeleteDraft.Failure(
                message = message,
                exception = ex)
    }

    override fun work(reporter: ProgressReporter<ComposerResult.DeleteDraft>): ComposerResult.DeleteDraft? {
        db.emailDao.deleteById(emailId, activeAccount.id)
        return ComposerResult.DeleteDraft.Success()
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { _ ->
        UIMessage(resId = R.string.failed_getting_emails)
    }
}
