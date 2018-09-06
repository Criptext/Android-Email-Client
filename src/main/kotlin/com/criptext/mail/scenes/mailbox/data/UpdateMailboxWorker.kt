package com.criptext.mail.scenes.mailbox.data

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.utils.EventHelper
import com.criptext.mail.utils.ServerErrorCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.whispersystems.libsignal.DuplicateMessageException

/**
 * Created by sebas on 3/22/18.
 */

class UpdateMailboxWorker(
        signalClient: SignalClient,
        private val dbEvents: EventLocalDB,
        activeAccount: ActiveAccount,
        private val loadedThreadsCount: Int,
        private val label: Label,
        httpClient: HttpClient,
        storage: KeyValueStorage,
        override val publishFn: (
                MailboxResult.UpdateMailbox) -> Unit)
    : BackgroundWorker<MailboxResult.UpdateMailbox> {


    override val canBeParallelized = false
    private val apiClient = MailboxAPIClient(httpClient, activeAccount.jwt)

    private val eventHelper = EventHelper(dbEvents, httpClient, activeAccount, signalClient, true)

    override fun catchException(ex: Exception): MailboxResult.UpdateMailbox =
        if(ex is ServerErrorException) {
            when {
                ex.errorCode == ServerErrorCodes.Unauthorized ->
                    MailboxResult.UpdateMailbox.Unauthorized(label, UIMessage(R.string.device_removed_remotely_exception), ex)
                ex.errorCode == ServerErrorCodes.Forbidden ->
                    MailboxResult.UpdateMailbox.Forbidden(label, UIMessage(R.string.device_removed_remotely_exception), ex)
                else -> MailboxResult.UpdateMailbox.Failure(label, createErrorMessage(ex), ex)
            }
        }
        else MailboxResult.UpdateMailbox.Failure(label, createErrorMessage(ex), ex)


    private fun processFailure(failure: Result.Failure<List<EmailPreview>, Exception>): MailboxResult.UpdateMailbox {
        return if (failure.error is EventHelper.NothingNewException)
            MailboxResult.UpdateMailbox.Success(
                    mailboxLabel = label,
                    isManual = true,
                    mailboxThreads = null)
        else
            catchException(failure.error)
    }

    override fun work(reporter: ProgressReporter<MailboxResult.UpdateMailbox>)
            : MailboxResult.UpdateMailbox? {
        eventHelper.setupForMailbox(label, loadedThreadsCount)
        val operationResult = eventHelper.fetchPendingEvents()
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
                .flatMap(eventHelper.parseEvents)
                .flatMap(eventHelper.processEvents)

        checkTrashDates()

        return when(operationResult) {
            is Result.Success -> {
                return MailboxResult.UpdateMailbox.Success(
                        mailboxLabel = label,
                        isManual = true,
                        mailboxThreads = operationResult.value
                )
            }

            is Result.Failure -> processFailure(operationResult)
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private fun checkTrashDates(){
        val threadIds = dbEvents.getThreadIdsFromTrashExpiredEmails()
        if(threadIds.isNotEmpty()){
            Result.of { apiClient.postThreadDeletedPermanentlyEvent(threadIds) }
                    .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
                    .flatMap { Result.of { dbEvents.updateDeleteThreadPermanently(threadIds) } }
        }
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when(ex) {
            is ServerErrorException -> UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
            is DuplicateMessageException ->
                UIMessage(resId = R.string.email_already_decrypted)
            else -> {
                UIMessage(resId = R.string.failed_getting_emails)
            }
        }
    }
}
