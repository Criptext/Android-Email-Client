package com.email.scenes.emaildetail.workers

import com.email.R
import com.email.SecureEmail
import com.email.api.HttpClient
import com.email.api.HttpErrorHandlingHelper
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.EmailDetailLocalDB
import com.email.db.models.ActiveAccount
import com.email.db.models.EmailLabel
import com.email.db.models.FullEmail
import com.email.db.models.Label
import com.email.scenes.emaildetail.data.EmailDetailAPIClient
import com.email.scenes.emaildetail.data.EmailDetailResult
import com.email.scenes.label_chooser.SelectedLabels
import com.email.scenes.label_chooser.data.LabelWrapper
import com.email.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError

/**
 * Created by sebas on 04/05/18.
 */

class MoveEmailThreadWorker(
        private val db: EmailDetailLocalDB,
        private val chosenLabel: String?,
        private val threadId: String,
        private val currentLabel: Label,
        httpClient: HttpClient,
        activeAccount: ActiveAccount,
        override val publishFn: (
                EmailDetailResult.MoveEmailThread) -> Unit)
    : BackgroundWorker<EmailDetailResult.MoveEmailThread> {

    private val apiClient = EmailDetailAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = false


    override fun catchException(ex: Exception): EmailDetailResult.MoveEmailThread {

        val message = createErrorMessage(ex)
        return EmailDetailResult.MoveEmailThread.Failure(
                message = message,
                exception = ex)
    }

    override fun work(reporter: ProgressReporter<EmailDetailResult.MoveEmailThread>): EmailDetailResult.MoveEmailThread? {

        val rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(currentLabel).map { it.id }
        val emailIds = db.getFullEmailsFromThreadId(threadId, rejectedLabels).map {
            it.email.id
        }

        if(chosenLabel == null){
            val result = Result.of {
                apiClient.postThreadDeletedPermanentlyEvent(listOf(threadId))
            }.mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)

            return when (result){
                is Result.Success -> {
                    //It means the threads will be deleted permanently
                    db.deleteRelationByEmailIds(emailIds = emailIds)
                    db.deleteThread(threadId)
                    EmailDetailResult.MoveEmailThread.Success(threadId)
                }
                is Result.Failure -> {
                    catchException(result.error)
                }
            }

        }
        val selectedLabels = SelectedLabels()
        selectedLabels.add(LabelWrapper(db.getLabelByName(chosenLabel)))

        val result = Result.of { apiClient.postThreadLabelChangedEvent(listOf(threadId),
                listOf(currentLabel.text), selectedLabels.toList().map { it.text }) }
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)

        return when (result) {
            is Result.Success -> {
                if(currentLabel == Label.defaultItems.trash && chosenLabel == SecureEmail.LABEL_SPAM){
                    //Mark as spam from trash
                    db.deleteRelationByLabelAndEmailIds(labelId = Label.defaultItems.trash.id,
                            emailIds = emailIds)
                }
                val emailLabels = arrayListOf<EmailLabel>()
                emailIds.flatMap{ emailId ->
                    selectedLabels.toIDs().map{ labelId ->
                        emailLabels.add(EmailLabel(
                                emailId = emailId,
                                labelId = labelId))
                    }
                }
                db.createLabelEmailRelations(emailLabels)
                EmailDetailResult.MoveEmailThread.Success(threadId)
            }
            is Result.Failure -> {
                catchException(result.error)
            }
        }
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        UIMessage(resId = R.string.failed_getting_emails)
    }
}
