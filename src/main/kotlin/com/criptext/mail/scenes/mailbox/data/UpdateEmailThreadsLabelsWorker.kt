package com.criptext.mail.scenes.mailbox.data

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.EmailLabel
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.label_chooser.SelectedLabels
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError

/**
 * Created by sebas on 04/05/18.
 */

class UpdateEmailThreadsLabelsWorker(
        private val db: MailboxLocalDB,
        private val selectedLabels: SelectedLabels,
        private val selectedThreadIds: List<String>,
        private val currentLabel: Label,
        private val shouldRemoveCurrentLabel: Boolean,
        httpClient: HttpClient,
        activeAccount: ActiveAccount,
        override val publishFn: (
                MailboxResult.UpdateEmailThreadsLabelsRelations) -> Unit)
    : BackgroundWorker<MailboxResult.UpdateEmailThreadsLabelsRelations> {

    private val defaultItems = Label.DefaultItems()
    override val canBeParallelized = false

    private val apiClient = MailboxAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): MailboxResult.UpdateEmailThreadsLabelsRelations {
        if(ex is ServerErrorException && ex.errorCode == 401)
            return MailboxResult.UpdateEmailThreadsLabelsRelations.Unauthorized(UIMessage(R.string.device_removed_remotely_exception))
        val message = createErrorMessage(ex)
        return MailboxResult.UpdateEmailThreadsLabelsRelations.Failure(
                message = message,
                exception = ex)
    }

    private fun removeCurrentLabelFromEmails(emailIds: List<Long>) {
        if(currentLabel == defaultItems.starred
          || currentLabel == defaultItems.sent)
            db.deleteRelationByLabelAndEmailIds(Label.defaultItems.inbox.id, emailIds)
        else
            db.deleteRelationByLabelAndEmailIds(currentLabel.id, emailIds)
    }

    private fun updateLabelEmailRelations(emailIds: List<Long>) {
        db.deleteRelationByEmailIds(emailIds = emailIds)

        val emailLabels =
                emailIds.flatMap { emailId ->
                    selectedLabels.toIDs().map { labelId ->
                        EmailLabel(emailId = emailId, labelId = labelId)
                    }
                }
        db.createLabelEmailRelations(emailLabels)
    }

    override fun work(reporter: ProgressReporter<MailboxResult.UpdateEmailThreadsLabelsRelations>)
            : MailboxResult.UpdateEmailThreadsLabelsRelations? {

        val removedLabels = if(currentLabel == defaultItems.starred
                || currentLabel == defaultItems.sent) listOf(Label.defaultItems.inbox.text)
        else
            listOf(currentLabel.text)

        val rejectedLabels = defaultItems.rejectedLabelsByMailbox(currentLabel).map { it.id }
        val emailIds = selectedThreadIds.flatMap { threadId ->
            db.getEmailsByThreadId(threadId, rejectedLabels).map { it.id }
        }

        val result = Result.of {
            apiClient.postThreadLabelChangedEvent(selectedThreadIds, removedLabels,
                    selectedLabels.toList().map { it.text })}
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)

        return when(result){
            is Result.Success -> {
                if(shouldRemoveCurrentLabel)
                    removeCurrentLabelFromEmails(emailIds)
                else
                    updateLabelEmailRelations(emailIds)
                MailboxResult.UpdateEmailThreadsLabelsRelations.Success()
            }
            is Result.Failure -> {
                catchException(result.error)
            }
        }
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is ServerErrorException -> UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
            else -> UIMessage(resId = R.string.failed_getting_emails)
        }
    }
}
