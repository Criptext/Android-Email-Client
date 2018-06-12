package com.email.scenes.composer.data

import com.email.R
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.ComposerLocalDB
import com.email.db.EmailDetailLocalDB
import com.email.db.MailFolders
import com.email.db.models.EmailLabel
import com.email.db.models.Label
import com.email.scenes.emaildetail.data.EmailDetailResult
import com.email.scenes.label_chooser.SelectedLabels
import com.email.scenes.label_chooser.data.LabelWrapper
import com.email.utils.UIMessage

/**
 * Created by danieltigse on 5/6/18.
 */

class DeleteDraftWorker(
        private val db: ComposerLocalDB,
        private val emailId: Long,
        override val publishFn: (
                ComposerResult.DeleteDraft) -> Unit)
    : BackgroundWorker<ComposerResult.DeleteDraft> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): ComposerResult.DeleteDraft{
        val message = createErrorMessage(ex)
        return ComposerResult.DeleteDraft.Failure(
                message = message,
                exception = ex)
    }

    override fun work(reporter: ProgressReporter<ComposerResult.DeleteDraft>): ComposerResult.DeleteDraft? {
        db.emailDao.deleteById(emailId)
        return ComposerResult.DeleteDraft.Success()
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        UIMessage(resId = R.string.failed_getting_emails)
    }
}
