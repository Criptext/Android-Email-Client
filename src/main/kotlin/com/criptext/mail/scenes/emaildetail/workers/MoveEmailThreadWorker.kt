package com.criptext.mail.scenes.emaildetail.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
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
import com.criptext.mail.utils.ServerErrorCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
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


    override fun catchException(ex: Exception): EmailDetailResult.MoveEmailThread =
        if(ex is ServerErrorException) {
            when {
                ex.errorCode == ServerErrorCodes.Unauthorized ->
                    EmailDetailResult.MoveEmailThread.Unauthorized(UIMessage(R.string.device_removed_remotely_exception))
                ex.errorCode == ServerErrorCodes.Forbidden ->
                    EmailDetailResult.MoveEmailThread.Forbidden()
                else -> EmailDetailResult.MoveEmailThread.Failure(
                        message = createErrorMessage(ex),
                        exception = ex)
            }
        }
        else EmailDetailResult.MoveEmailThread.Failure(
                message = createErrorMessage(ex),
                exception = ex)


    override fun work(reporter: ProgressReporter<EmailDetailResult.MoveEmailThread>): EmailDetailResult.MoveEmailThread? {

        val rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(currentLabel).map { it.id }
        val emailIds = db.getFullEmailsFromThreadId(threadId = threadId, rejectedLabels = rejectedLabels).map {
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
                if(chosenLabel == Label.LABEL_TRASH){
                    db.setTrashDate(emailIds)
                }
                if(currentLabel == Label.defaultItems.trash && chosenLabel == Label.LABEL_SPAM){
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
        when(ex){
            is ServerErrorException -> UIMessage(resId = R.string.server_error_exception)
            else -> UIMessage(resId = R.string.failed_getting_emails)
        }
    }
}
