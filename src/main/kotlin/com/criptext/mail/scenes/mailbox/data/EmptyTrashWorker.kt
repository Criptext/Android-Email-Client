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

class EmptyTrashWorker(
        private val db: MailboxLocalDB,
        httpClient: HttpClient,
        activeAccount: ActiveAccount,
        override val publishFn: (
                MailboxResult.EmptyTrash) -> Unit)
    : BackgroundWorker<MailboxResult.EmptyTrash> {

    private val apiClient = MailboxAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.EmptyTrash =
            if(ex is ServerErrorException) {
                when {
                    ex.errorCode == ServerErrorCodes.Unauthorized ->
                        MailboxResult.EmptyTrash.Unauthorized(UIMessage(R.string.device_removed_remotely_exception))
                    ex.errorCode == ServerErrorCodes.Forbidden ->
                        MailboxResult.EmptyTrash.Forbidden()
                    else -> MailboxResult.EmptyTrash.Failure(createErrorMessage(ex))
                }
            }
            else MailboxResult.EmptyTrash.Failure(createErrorMessage(ex))

    override fun work(reporter: ProgressReporter<MailboxResult.EmptyTrash>)
            : MailboxResult.EmptyTrash? {
        val metadataKeys = db.getEmailMetadataKeysFromLabel(Label.LABEL_TRASH)
        val result = Result.of {
            apiClient.postEmailDeleteEvent(metadataKeys) }
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
                .flatMap { Result.of { db.deleteEmail(metadataKeys) } }
        return when (result) {
            is Result.Success -> {
                MailboxResult.EmptyTrash.Success()
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
            else -> UIMessage(resId = R.string.failed_empty_trash)
        }
    }
}
