package com.criptext.mail.scenes.emaildetail.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.EmailDetailLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.EmailLabel
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.emaildetail.data.EmailDetailAPIClient
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.scenes.label_chooser.SelectedLabels
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError

/**
 * Created by danieltigse on 05/01/18.
 */

class UpdateEmailThreadLabelsWorker(
        httpClient: HttpClient,
        activeAccount: ActiveAccount,
        private val db: EmailDetailLocalDB,
        private val currentLabel: Label,
        private val selectedLabels: SelectedLabels,
        private val threadId: String,
        private val removeCurrentLabel: Boolean,
        override val publishFn: (
                EmailDetailResult.UpdateEmailThreadsLabelsRelations) -> Unit)
    : BackgroundWorker<EmailDetailResult.UpdateEmailThreadsLabelsRelations> {

    private val apiClient = EmailDetailAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): EmailDetailResult.UpdateEmailThreadsLabelsRelations {

        val message = createErrorMessage(ex)
        return EmailDetailResult.UpdateEmailThreadsLabelsRelations.Failure(
                message = message,
                exception = ex)
    }

    override fun work(reporter: ProgressReporter<EmailDetailResult.UpdateEmailThreadsLabelsRelations>)
            : EmailDetailResult.UpdateEmailThreadsLabelsRelations? {


        val selectedLabelsList = selectedLabels.toList().map { it.label }
        val rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(currentLabel).map { it.id }
        val systemLabels = db.getLabelsByName(Label.defaultItems.toList().map { it.text })
                .filter { !rejectedLabels.contains(it.id) }
                .filter { it.text != Label.LABEL_STARRED }
        val emails = db.getFullEmailsFromThreadId(threadId = threadId, rejectedLabels = rejectedLabels)
        val emailIds = emails.map { it.email.id }
        val removedLabels = if(currentLabel == Label.defaultItems.starred
                || currentLabel == Label.defaultItems.sent) listOf(Label.defaultItems.inbox.text)
        else
            listOf(currentLabel.text)

        val peerSelectedLabels = selectedLabels.toList()
                .filter { it.text != currentLabel.text }
                .toList().map { it.text }
        val peerRemovedLabels = db.getLabelsFromThreadId(threadId)
                .filter { !selectedLabelsList.contains(it) }
                .filter { (!systemLabels.contains(it)) }
                .map { it.text }
                .toMutableList()



        val result =
            if(removeCurrentLabel){
                if(currentLabel == Label.defaultItems.spam){
                    peerRemovedLabels.removeAll(peerRemovedLabels)
                    peerRemovedLabels.add(Label.LABEL_SPAM)
                }else
                    peerRemovedLabels.add(currentLabel.text)
                Result.of {
                    apiClient.postEmailLabelChangedEvent(emails.map { it.email.metadataKey },
                            peerRemovedLabels,
                            peerSelectedLabels)
                }
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
                .flatMap { Result.of {
                    if(currentLabel == Label.defaultItems.starred
                            || currentLabel == Label.defaultItems.sent){
                        db.deleteRelationByLabelAndEmailIds(Label.defaultItems.inbox.id, emailIds)
                    }
                    else{
                        db.deleteRelationByLabelAndEmailIds(currentLabel.id, emailIds)
                    }

                } }

            } else {
                Result.of {
                    apiClient.postEmailLabelChangedEvent(emails.map { it.email.metadataKey },
                            peerRemovedLabels,
                            peerSelectedLabels)
                }
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
                .flatMap {
                    Result.of {
                        db.deleteRelationByEmailIds(emailIds = emailIds)

                        val emailLabels = arrayListOf<EmailLabel>()
                        emailIds.flatMap { emailId ->
                            selectedLabels.toIDs().map { labelId ->
                                emailLabels.add(EmailLabel(
                                        emailId = emailId,
                                        labelId = labelId))
                            }
                        }
                        db.createLabelEmailRelations(emailLabels)
                    }
                }
            }

        return when(result){
            is Result.Success -> {
                val labels = db.getLabelsFromThreadId(threadId)
                EmailDetailResult.UpdateEmailThreadsLabelsRelations.Success(threadId, labels)
            }
            is Result.Failure -> {
                catchException(result.error)
            }
        }

    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { _ ->
        UIMessage(resId = R.string.failed_getting_emails)
    }
}
