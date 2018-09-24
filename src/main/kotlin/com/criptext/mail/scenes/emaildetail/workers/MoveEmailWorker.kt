package com.criptext.mail.scenes.emaildetail.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.EmailDetailLocalDB
import com.criptext.mail.db.dao.EmailDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.EmailLabel
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.emaildetail.data.EmailDetailAPIClient
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.scenes.label_chooser.SelectedLabels
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError

/**
 * Created by danieltigse on 5/6/18.
 */

class MoveEmailWorker(
        private val db: EmailDetailLocalDB,
        private val emailDao: EmailDao,
        private val chosenLabel: String?,
        private val emailId: Long,
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

        val emailIds = listOf(emailId)

        if(chosenLabel == null){
            //It means the email will be deleted permanently
            val result = Result.of {apiClient.postEmailDeleteEvent(
                    emailDao.getAllEmailsbyId(emailIds).map { it.metadataKey })}
                    .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
            return when (result) {
                is Result.Success -> {
                    db.deleteEmail(emailId)
                    EmailDetailResult.MoveEmailThread.Success(null)
                }
                is Result.Failure -> {
                    val message = createErrorMessage(result.error)
                    EmailDetailResult.MoveEmailThread.Failure(message = message, exception = result.error)
                }
            }
        }

        val selectedLabels = SelectedLabels()
        selectedLabels.add(LabelWrapper(db.getLabelByName(chosenLabel)))
        val peerRemoveLabels = if(currentLabel == Label.defaultItems.trash
                || currentLabel == Label.defaultItems.spam)
            listOf(currentLabel.text)
        else
            emptyList()

        val result =  Result.of{apiClient.postEmailLabelChangedEvent(
                emailDao.getAllEmailsbyId(emailIds).map { it.metadataKey },
                peerRemoveLabels, selectedLabels.toList().map { it.text })}
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)

        return when (result) {
            is Result.Success -> {
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

                if(chosenLabel == Label.LABEL_TRASH){
                    db.setTrashDate(emailIds)
                }

                EmailDetailResult.MoveEmailThread.Success(null)
            }
            is Result.Failure -> {
                val message = createErrorMessage(result.error)
                EmailDetailResult.MoveEmailThread.Failure(message = message, exception = result.error)
            }
        }

    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when(ex){
            is ServerErrorException -> {
                when {
                    ex.errorCode == 401 -> UIMessage(resId = R.string.device_removed_remotely_exception)
                    else -> UIMessage(resId = R.string.server_error_exception)
                }
            }
            else -> UIMessage(resId = R.string.failed_getting_emails)
        }
    }
}
