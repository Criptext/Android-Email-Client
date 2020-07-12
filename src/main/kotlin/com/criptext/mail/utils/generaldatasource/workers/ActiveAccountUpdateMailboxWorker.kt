package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.api.*
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.PendingEventDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.mailbox.data.MailboxAPIClient
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.utils.*
import com.criptext.mail.utils.eventhelper.EventHelper
import com.criptext.mail.utils.eventhelper.EventHelperResultData
import com.criptext.mail.utils.eventhelper.EventLoader
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.peerdata.PeerDeleteEmailData
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.whispersystems.libsignal.DuplicateMessageException


class ActiveAccountUpdateMailboxWorker(
        private val account: ActiveAccount,
        private val db: AppDatabase,
        private val dbEvents: EventLocalDB,
        val pendingEventDao: PendingEventDao,
        private val label: Label,
        private val httpClient: HttpClient,
        private val storage: KeyValueStorage,
        private val accountDao: AccountDao,
        override val publishFn: (
                GeneralResult.ActiveAccountUpdateMailbox) -> Unit)
    : BackgroundWorker<GeneralResult.ActiveAccountUpdateMailbox> {


    override val canBeParallelized = false

    private lateinit var activeAccount: ActiveAccount

    private lateinit var signalClient: SignalClient.Default
    private lateinit var apiClient: GeneralAPIClient
    private lateinit var mailboxApiClient: MailboxAPIClient

    private lateinit var eventHelper: EventHelper
    private lateinit var peerEventsApiHandler: PeerEventsApiHandler.Default

    private var shouldCallAgain = false

    override fun catchException(ex: Exception): GeneralResult.ActiveAccountUpdateMailbox =
        if(ex is ServerErrorException) {
            when {
                ex.errorCode == ServerCodes.Unauthorized ->
                    GeneralResult.ActiveAccountUpdateMailbox.Unauthorized(label, createErrorMessage(ex), ex)
                ex.errorCode == ServerCodes.SessionExpired ->
                    GeneralResult.ActiveAccountUpdateMailbox.SessionExpired()
                ex.errorCode == ServerCodes.Forbidden ->
                    GeneralResult.ActiveAccountUpdateMailbox.Forbidden(label, UIMessage(R.string.device_removed_remotely_exception), ex)
                ex.errorCode == ServerCodes.EnterpriseAccountSuspended ->
                    GeneralResult.ActiveAccountUpdateMailbox.EnterpriseSuspended(label)
                else -> GeneralResult.ActiveAccountUpdateMailbox.Failure(label, createErrorMessage(ex), ex)
            }
        }
        else GeneralResult.ActiveAccountUpdateMailbox.Failure(label, createErrorMessage(ex), ex)


    private fun processFailure(failure: Result.Failure<EventHelperResultData, Exception>): GeneralResult.ActiveAccountUpdateMailbox {
        return if (failure.error is EventHelper.NothingNewException)
            GeneralResult.ActiveAccountUpdateMailbox.Success(
                    mailboxLabel = label,
                    isManual = true,
                    data = null,
                    shouldNotify = false)
        else
            catchException(failure.error)
    }

    private fun setup(): Boolean {
        activeAccount = account
        signalClient = SignalClient.Default(SignalStoreCriptext(db, activeAccount))
        apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)
        mailboxApiClient = MailboxAPIClient(httpClient, activeAccount.jwt)

        eventHelper = EventHelper(dbEvents, httpClient, storage, activeAccount, signalClient, true)
        peerEventsApiHandler = PeerEventsApiHandler.Default(activeAccount, pendingEventDao,
                storage, accountDao)
        return true
    }

    override fun work(reporter: ProgressReporter<GeneralResult.ActiveAccountUpdateMailbox>)
            : GeneralResult.ActiveAccountUpdateMailbox? {
        if(!setup()) return  GeneralResult.ActiveAccountUpdateMailbox.Failure(label, createErrorMessage(Exception()), Exception())
        eventHelper.setupForMailbox(label)
        val operationResult = workOperation()

        checkTrashDates()
        UIUtils.checkForCacheCleaning(storage, dbEvents.getCacheDir(), activeAccount)


        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(operationResult)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            operationResult


        return when(finalResult) {
            is Result.Success -> {
                return if(shouldCallAgain){
                    GeneralResult.ActiveAccountUpdateMailbox.SuccessAndRepeat(
                            mailboxLabel = label,
                            isManual = true,
                            data = finalResult.value,
                            shouldNotify = finalResult.value.shouldNotify
                    )
                }else {
                    GeneralResult.ActiveAccountUpdateMailbox.Success(
                            mailboxLabel = label,
                            isManual = true,
                            data = finalResult.value,
                            shouldNotify = finalResult.value.shouldNotify
                    )
                }
            }

            is Result.Failure -> processFailure(finalResult)
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private fun workOperation() : Result<EventHelperResultData, Exception>  {
        val requestEvents = EventLoader.getEvents(mailboxApiClient)
        shouldCallAgain = (requestEvents as? Result.Success)?.value?.second ?: false
        return requestEvents
                .flatMap(eventHelper.processEvents)
    }

    private fun newRetryWithNewSessionOperation()
            : Result<EventHelperResultData, Exception> {
        val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient,
                activeAccount, storage, accountDao)
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when(refreshOperation){
            is Result.Success -> {
                val account = ActiveAccount.loadFromDB(accountDao.getAccountById(activeAccount.id)!!)!!
                apiClient.token = account.jwt
                mailboxApiClient.token = account.jwt

                eventHelper = EventHelper(dbEvents, httpClient, storage, account, signalClient, true)
                eventHelper.setupForMailbox(label)
                workOperation()
            }
            is Result.Failure -> {
                Result.of { throw refreshOperation.error }
            }
        }
    }

    private fun checkTrashDates(){
        val emailIds = dbEvents.getEmailIdsFromTrashExpiredEmails(activeAccount.id)
        if(emailIds.isNotEmpty()){
            Result.of { dbEvents.updateDeleteEmailPermanentlyByIds(emailIds, activeAccount) }
            emailIds.asSequence().batch(PeerEventsApiHandler.BATCH_SIZE).forEach { batch ->
                peerEventsApiHandler.enqueueEvent(PeerDeleteEmailData(batch).toJSON())
            }
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
