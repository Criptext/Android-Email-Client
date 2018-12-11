package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.PeerEventsApiHandler
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.PendingEventDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.mailbox.data.MailboxAPIClient
import com.criptext.mail.scenes.mailbox.data.UpdateBannerData
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.utils.EventHelper
import com.criptext.mail.utils.EventLoader
import com.criptext.mail.utils.ServerErrorCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.peerdata.PeerDeleteEmailData
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.whispersystems.libsignal.DuplicateMessageException

class UpdateMailboxWorker(
        private val signalClient: SignalClient,
        private val dbEvents: EventLocalDB,
        val pendingEventDao: PendingEventDao,
        private val activeAccount: ActiveAccount,
        private val loadedThreadsCount: Int,
        private val label: Label,
        private val httpClient: HttpClient,
        private val storage: KeyValueStorage,
        private val accountDao: AccountDao,
        override val publishFn: (
                GeneralResult.UpdateMailbox) -> Unit)
    : BackgroundWorker<GeneralResult.UpdateMailbox> {


    override val canBeParallelized = false
    private val apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)
    private val mailboxApiClient = MailboxAPIClient(httpClient, activeAccount.jwt)

    private var eventHelper = EventHelper(dbEvents, httpClient, activeAccount, signalClient, true)
    private val peerEventsApiHandler = PeerEventsApiHandler.Default(httpClient, activeAccount, pendingEventDao, storage, accountDao)

    override fun catchException(ex: Exception): GeneralResult.UpdateMailbox =
        if(ex is ServerErrorException) {
            when {
                ex.errorCode == ServerErrorCodes.DeviceRemoved ->
                    GeneralResult.UpdateMailbox.Unauthorized(label, UIMessage(R.string.device_removed_remotely_exception), ex)
                ex.errorCode == ServerErrorCodes.Forbidden ->
                    GeneralResult.UpdateMailbox.Forbidden(label, UIMessage(R.string.device_removed_remotely_exception), ex)
                else -> GeneralResult.UpdateMailbox.Failure(label, createErrorMessage(ex), ex)
            }
        }
        else GeneralResult.UpdateMailbox.Failure(label, createErrorMessage(ex), ex)


    private fun processFailure(failure: Result.Failure<Pair<List<EmailPreview>, UpdateBannerData?>, Exception>): GeneralResult.UpdateMailbox {
        return if (failure.error is EventHelper.NothingNewException)
            GeneralResult.UpdateMailbox.Success(
                    mailboxLabel = label,
                    isManual = true,
                    mailboxThreads = null,
                    updateBannerData = null)
        else
            catchException(failure.error)
    }

    override fun work(reporter: ProgressReporter<GeneralResult.UpdateMailbox>)
            : GeneralResult.UpdateMailbox? {
        eventHelper.setupForMailbox(label, loadedThreadsCount)
        val operationResult = workOperation()

        checkTrashDates()


        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(operationResult)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            operationResult


        return when(finalResult) {
            is Result.Success -> {
                return GeneralResult.UpdateMailbox.Success(
                        mailboxLabel = label,
                        isManual = true,
                        mailboxThreads = finalResult.value.first,
                        updateBannerData = finalResult.value.second
                )
            }

            is Result.Failure -> processFailure(finalResult)
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private fun workOperation() : Result<Pair<List<EmailPreview>, UpdateBannerData?>, Exception> =
            EventLoader.getEvents(mailboxApiClient)
            .flatMap(eventHelper.processEvents)

    private fun newRetryWithNewSessionOperation()
            : Result<Pair<List<EmailPreview>, UpdateBannerData?>, Exception> {
        val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient,
                activeAccount, storage, accountDao)
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when(refreshOperation){
            is Result.Success -> {
                val account = ActiveAccount.loadFromStorage(storage)!!
                apiClient.token = account.jwt
                mailboxApiClient.token = account.jwt

                eventHelper = EventHelper(dbEvents, httpClient, account, signalClient, true)
                eventHelper.setupForMailbox(label, loadedThreadsCount)
                workOperation()
            }
            is Result.Failure -> {
                Result.of { throw refreshOperation.error }
            }
        }
    }

    private fun checkTrashDates(){
        val emailIds = dbEvents.getThreadIdsFromTrashExpiredEmails()
        if(emailIds.isNotEmpty()){
            Result.of { dbEvents.updateDeleteEmailPermanently(emailIds) }
            peerEventsApiHandler.enqueueEvent(PeerDeleteEmailData(emailIds).toJSON())
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
