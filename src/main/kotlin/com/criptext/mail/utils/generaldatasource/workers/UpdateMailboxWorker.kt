package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.api.*
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.AppDatabase_Impl
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
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.utils.*
import com.criptext.mail.utils.file.FileUtils
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.peerdata.PeerDeleteEmailData
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import com.squareup.picasso.Picasso
import org.whispersystems.libsignal.DuplicateMessageException
import java.io.File


class UpdateMailboxWorker(
        private val isActiveAccount: Boolean,
        db: AppDatabase,
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

    private val signalClient = SignalClient.Default(SignalStoreCriptext(db, activeAccount))
    private val apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)
    private val mailboxApiClient = MailboxAPIClient(httpClient, activeAccount.jwt)

    private var eventHelper = EventHelper(dbEvents, httpClient, storage, activeAccount, signalClient, true)
    private val peerEventsApiHandler = PeerEventsApiHandler.Default(httpClient, activeAccount, pendingEventDao, storage, accountDao)

    private var shouldCallAgain = false

    override fun catchException(ex: Exception): GeneralResult.UpdateMailbox =
        if(ex is ServerErrorException) {
            when {
                ex.errorCode == ServerCodes.Unauthorized ->
                    GeneralResult.UpdateMailbox.Unauthorized(isActiveAccount, label, UIMessage(R.string.device_removed_remotely_exception), ex)
                ex.errorCode == ServerCodes.Forbidden ->
                    GeneralResult.UpdateMailbox.Forbidden(isActiveAccount, label, UIMessage(R.string.device_removed_remotely_exception), ex)
                else -> GeneralResult.UpdateMailbox.Failure(isActiveAccount, label, createErrorMessage(ex), ex)
            }
        }
        else GeneralResult.UpdateMailbox.Failure(isActiveAccount, label, createErrorMessage(ex), ex)


    private fun processFailure(failure: Result.Failure<EventHelperResultData, Exception>): GeneralResult.UpdateMailbox {
        return if (failure.error is EventHelper.NothingNewException)
            GeneralResult.UpdateMailbox.Success(
                    mailboxLabel = label,
                    isManual = true,
                    mailboxThreads = null,
                    updateBannerData = null,
                    syncEventsList = listOf(),
                    shouldNotify = false,
                    isActiveAccount = isActiveAccount)
        else
            catchException(failure.error)
    }

    override fun work(reporter: ProgressReporter<GeneralResult.UpdateMailbox>)
            : GeneralResult.UpdateMailbox? {
        eventHelper.setupForMailbox(label, loadedThreadsCount)
        val operationResult = workOperation()

        checkTrashDates()
        checkForCacheCleaning()


        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(operationResult)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            operationResult


        return when(finalResult) {
            is Result.Success -> {
                return if(shouldCallAgain){
                    GeneralResult.UpdateMailbox.SuccessAndRepeat(
                            mailboxLabel = label,
                            isManual = true,
                            mailboxThreads = finalResult.value.emailPreviews,
                            updateBannerData = finalResult.value.updateBannerData,
                            syncEventsList = finalResult.value.deviceInfo,
                            shouldNotify = finalResult.value.shouldNotify,
                            isActiveAccount = isActiveAccount
                    )
                }else {
                    GeneralResult.UpdateMailbox.Success(
                            mailboxLabel = label,
                            isManual = true,
                            mailboxThreads = finalResult.value.emailPreviews,
                            updateBannerData = finalResult.value.updateBannerData,
                            syncEventsList = finalResult.value.deviceInfo,
                            shouldNotify = finalResult.value.shouldNotify,
                            isActiveAccount = isActiveAccount
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
                activeAccount, storage, accountDao, isActiveAccount)
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when(refreshOperation){
            is Result.Success -> {
                val account = ActiveAccount.loadFromDB(accountDao.getAccountByRecipientId(activeAccount.recipientId)!!)!!
                apiClient.token = account.jwt
                mailboxApiClient.token = account.jwt

                eventHelper = EventHelper(dbEvents, httpClient, storage, account, signalClient, true)
                eventHelper.setupForMailbox(label, loadedThreadsCount)
                workOperation()
            }
            is Result.Failure -> {
                Result.of { throw refreshOperation.error }
            }
        }
    }

    private fun checkTrashDates(){
        val emailIds = dbEvents.getThreadIdsFromTrashExpiredEmails(activeAccount.id)
        if(emailIds.isNotEmpty()){
            Result.of { dbEvents.updateDeleteEmailPermanently(emailIds, activeAccount) }
            peerEventsApiHandler.enqueueEvent(PeerDeleteEmailData(emailIds).toJSON())
        }
    }

    private fun checkForCacheCleaning() {
        val currentMillis = System.currentTimeMillis()
        val millisInADays = (24 * 60 * 60 * 1000).toLong()
        val savedTime = storage.getLong(KeyValueStorage.StringKey.CacheResetTimestamp, 0L)
        if(savedTime < currentMillis - millisInADays){
            Picasso.get().invalidate(Hosts.restApiBaseUrl.plus("/user/avatar/${activeAccount.recipientId}"))
            storage.putLong(KeyValueStorage.StringKey.CacheResetTimestamp, currentMillis)
            clearImageDiskCache()
        }
    }

    private fun clearImageDiskCache(): Boolean {
        val cache = File(dbEvents.getCacheDir(), "picasso-cache")
        return if (cache.exists() && cache.isDirectory) {
            FileUtils.deleteDir(cache)
        } else false
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
