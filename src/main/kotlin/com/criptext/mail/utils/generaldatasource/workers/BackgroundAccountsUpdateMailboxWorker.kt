package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.api.*
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.PendingEventDao
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.mailbox.data.MailboxAPIClient
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.utils.*
import com.criptext.mail.utils.eventhelper.EventHelper
import com.criptext.mail.utils.eventhelper.EventHelperResultData
import com.criptext.mail.utils.eventhelper.EventLoader
import com.criptext.mail.utils.eventhelper.ParsedEvent
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.peerdata.PeerDeleteEmailData
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.whispersystems.libsignal.DuplicateMessageException


class BackgroundAccountsUpdateMailboxWorker(
        private val db: AppDatabase,
        private val dbEvents: EventLocalDB,
        val pendingEventDao: PendingEventDao,
        private val accounts: List<Account>,
        private val label: Label,
        private val httpClient: HttpClient,
        private val storage: KeyValueStorage,
        private val accountDao: AccountDao,
        override val publishFn: (
                GeneralResult.BackgroundAccountsUpdateMailbox) -> Unit)
    : BackgroundWorker<GeneralResult.BackgroundAccountsUpdateMailbox> {


    override val canBeParallelized = false

    private lateinit var activeAccount: ActiveAccount

    private lateinit var signalClient: SignalClient.Default
    private lateinit var apiClient: GeneralAPIClient
    private lateinit var mailboxApiClient: MailboxAPIClient

    private lateinit var eventHelper: EventHelper
    private lateinit var peerEventsApiHandler: PeerEventsApiHandler.Default

    private var shouldCallAgain = false

    private var shouldUpdateUI = false

    override fun catchException(ex: Exception): GeneralResult.BackgroundAccountsUpdateMailbox =
        if(ex is ServerErrorException) {
            when {
                ex.errorCode == ServerCodes.Unauthorized ->
                    GeneralResult.BackgroundAccountsUpdateMailbox.Unauthorized(label, UIMessage(R.string.device_removed_remotely_exception), ex)
                ex.errorCode == ServerCodes.Forbidden ->
                    GeneralResult.BackgroundAccountsUpdateMailbox.Forbidden(label, UIMessage(R.string.device_removed_remotely_exception), ex)
                ex.errorCode == ServerCodes.EnterpriseAccountSuspended ->
                    GeneralResult.BackgroundAccountsUpdateMailbox.EnterpriseSuspended(label)
                else -> GeneralResult.BackgroundAccountsUpdateMailbox.Failure(label, createErrorMessage(ex), ex)
            }
        }
        else GeneralResult.BackgroundAccountsUpdateMailbox.Failure(label, createErrorMessage(ex), ex)


    private fun processFailure(failure: Result.Failure<EventHelperResultData, Exception>): GeneralResult.BackgroundAccountsUpdateMailbox {
        return if (failure.error is EventHelper.NothingNewException)
            GeneralResult.BackgroundAccountsUpdateMailbox.Success(
                    mailboxLabel = label,
                    isManual = true,
                    updateBannerData = null,
                    syncEventsList = listOf(),
                    shouldUpdateUI = shouldUpdateUI)
        else
            catchException(failure.error)
    }

    private fun setup(account: Account): Boolean {
        activeAccount = ActiveAccount.loadFromDB(account)?: return false
        signalClient = SignalClient.Default(SignalStoreCriptext(db, activeAccount))
        apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)
        mailboxApiClient = MailboxAPIClient(httpClient, activeAccount.jwt)

        eventHelper = EventHelper(dbEvents, httpClient, storage, activeAccount, signalClient, true)
        peerEventsApiHandler = PeerEventsApiHandler.Default(httpClient, activeAccount, pendingEventDao, storage, accountDao)
        return true
    }

    override fun work(reporter: ProgressReporter<GeneralResult.BackgroundAccountsUpdateMailbox>)
            : GeneralResult.BackgroundAccountsUpdateMailbox? {

        var index = 0
        val deviceInfo = mutableListOf<DeviceInfo?>()
        while(index < accounts.size){
            if(setup(accounts[index])) {
                eventHelper.setupForMailbox(label)
                val operationResult = workOperation()
                val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(operationResult)

                val finalResult = if (sessionExpired)
                    newRetryWithNewSessionOperation()
                else
                    operationResult
                when (finalResult) {
                    is Result.Success -> {
                        shouldUpdateUI = shouldUpdateUI || true
                        if (!shouldCallAgain) {
                            val linkInfo = finalResult.value.parsedEvents.find { it is ParsedEvent.LinkDeviceInfo }
                            if(index == (accounts.size - 1)) {
                                if(linkInfo != null)
                                    deviceInfo.add((linkInfo as ParsedEvent.LinkDeviceInfo).deviceInfo)
                                val bannerData = finalResult.value.parsedEvents.find { it is ParsedEvent.BannerData }
                                return GeneralResult.BackgroundAccountsUpdateMailbox.Success(
                                        mailboxLabel = label,
                                        isManual = true,
                                        updateBannerData = (bannerData as? ParsedEvent.BannerData)?.updateBannerData,
                                        syncEventsList = deviceInfo,
                                        shouldUpdateUI = shouldUpdateUI
                                )
                            } else {
                                if(linkInfo != null)
                                    deviceInfo.add((linkInfo as ParsedEvent.LinkDeviceInfo).deviceInfo)
                                index++
                            }
                        }
                    }
                    is Result.Failure -> {
                        shouldUpdateUI = shouldUpdateUI || false
                        if(index == (accounts.size - 1))
                            processFailure(finalResult)
                        else {
                            index++
                        }
                    }
                }
            }
            index++
        }
        return GeneralResult.BackgroundAccountsUpdateMailbox.Success(
                mailboxLabel = label,
                isManual = true,
                updateBannerData = null,
                syncEventsList = listOf(),
                shouldUpdateUI = false)
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
