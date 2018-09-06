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
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.utils.ServerErrorCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError

/**
 * Created by sebas on 04/05/18.
 */

class MoveEmailThreadWorker(
        private val db: MailboxLocalDB,
        private val chosenLabel: String?,
        private val selectedThreadIds: List<String>,
        private val currentLabel: Label,
        httpClient: HttpClient,
        activeAccount: ActiveAccount,
        override val publishFn: (
                MailboxResult.MoveEmailThread) -> Unit)
    : BackgroundWorker<MailboxResult.MoveEmailThread> {

    private val apiClient = MailboxAPIClient(httpClient, activeAccount.jwt)

    private val defaultItems = Label.DefaultItems()
    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.MoveEmailThread =
            if(ex is ServerErrorException) {
                when {
                    ex.errorCode == ServerErrorCodes.Unauthorized ->
                        MailboxResult.MoveEmailThread.Unauthorized(UIMessage(R.string.device_removed_remotely_exception))
                    ex.errorCode == ServerErrorCodes.Forbidden ->
                        MailboxResult.MoveEmailThread.Forbidden()
                    else -> MailboxResult.MoveEmailThread.Failure(
                            message = createErrorMessage(ex),
                            exception = ex)
                }
            }
            else MailboxResult.MoveEmailThread.Failure(
                    message = createErrorMessage(ex),
                    exception = ex)


    private fun getLabelEmailRelationsFromEmailIds(emailIds: List<Long>, label: String): List<EmailLabel> {
        val selectedLabels = SelectedLabels()
        selectedLabels.add(LabelWrapper(db.getLabelByName(label)))


        return emailIds.flatMap{ emailId ->
                selectedLabels.toIDs().map{ labelId ->
                    EmailLabel(emailId = emailId, labelId = labelId)
                }
            }
    }

    override fun work(reporter: ProgressReporter<MailboxResult.MoveEmailThread>)
            : MailboxResult.MoveEmailThread? {

        if(chosenLabel == null){
            //It means the threads will be deleted permanently
            val result = Result.of {
                apiClient.postThreadDeletedPermanentlyEvent(selectedThreadIds) }
                    .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
                    .flatMap { Result.of { db.deleteThreads(threadIds = selectedThreadIds) } }
            return when (result) {
                is Result.Success -> {
                    MailboxResult.MoveEmailThread.Success()
                }
                is Result.Failure -> {
                    catchException(result.error)
                }
            }
        }

        val rejectedLabels = defaultItems.rejectedLabelsByMailbox(currentLabel).map { it.id }
        val emailIds = selectedThreadIds.flatMap { threadId ->
            db.getEmailsByThreadId(threadId, rejectedLabels).map { it.id }
        }

        val selectedLabels = SelectedLabels()
        selectedLabels.add(LabelWrapper(db.getLabelByName(chosenLabel)))

        val result = Result.of {
            apiClient.postThreadLabelChangedEvent(selectedThreadIds, listOf(currentLabel.text),
                selectedLabels.toList().map { it.text })}
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
                .flatMap { Result.of { if(currentLabel == Label.defaultItems.trash && chosenLabel == Label.LABEL_SPAM){
                    //Mark as spam from trash
                    db.deleteRelationByLabelAndEmailIds(labelId = defaultItems.trash.id,
                            emailIds = emailIds)
                    }
                    val labelEmails = getLabelEmailRelationsFromEmailIds(emailIds, chosenLabel)
                    db.createLabelEmailRelations(labelEmails)
                    if(chosenLabel == Label.LABEL_TRASH){
                        db.setTrashDate(emailIds)
                    }
                } }

        return when (result) {
            is Result.Success -> {
                MailboxResult.MoveEmailThread.Success()
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
