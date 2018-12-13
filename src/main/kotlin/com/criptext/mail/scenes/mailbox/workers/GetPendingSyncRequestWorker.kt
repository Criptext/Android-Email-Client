package com.criptext.mail.scenes.mailbox.workers

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.models.Event
import com.criptext.mail.api.models.TrustedDeviceInfo
import com.criptext.mail.api.models.UntrustedDeviceInfo
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.mailbox.data.MailboxAPIClient
import com.criptext.mail.scenes.mailbox.data.MailboxResult
import com.criptext.mail.utils.EventLoader
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.map
import com.github.kittinunf.result.mapError
import java.io.IOException

class GetPendingSyncRequestWorker(
        private val httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private val accountDao: AccountDao,
        override val publishFn: (
                MailboxResult.GetPendingSyncRequest) -> Unit)
    : BackgroundWorker<MailboxResult.GetPendingSyncRequest> {

    private val apiClient = MailboxAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.GetPendingSyncRequest =
            MailboxResult.GetPendingSyncRequest.Failure()

    override fun work(reporter: ProgressReporter<MailboxResult.GetPendingSyncRequest>)
            : MailboxResult.GetPendingSyncRequest? {
        val result = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(result)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            result

        return when (finalResult) {
            is Result.Success -> {
                MailboxResult.GetPendingSyncRequest.Success(finalResult.value)
            }
            is Result.Failure -> {
                catchException(finalResult.error)
            }
        }
    }

    private fun workOperation() : Result<TrustedDeviceInfo, Exception> = EventLoader.getEvents(apiClient)
            .flatMap(processEvents)

    private fun newRetryWithNewSessionOperation()
            : Result<TrustedDeviceInfo, Exception> {
        val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient, activeAccount, storage, accountDao)
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when(refreshOperation){
            is Result.Success -> {
                val account = ActiveAccount.loadFromStorage(storage)!!
                apiClient.token = account.jwt
                workOperation()
            }
            is Result.Failure -> {
                Result.of { throw refreshOperation.error }
            }
        }
    }


    private val processEvents: (List<Event>) -> Result<TrustedDeviceInfo, Exception> = { events ->
        Result.of {
            processLinkRequestEvents(events)
        }
    }

    private fun processLinkRequestEvents(events: List<Event>): TrustedDeviceInfo {
        val isDeviceLinkRequest: (Event) -> Boolean = { it.cmd == Event.Cmd.syncBeginRequest }
        val toIdAndDeviceInfoPair: (Event) -> Pair<Long, TrustedDeviceInfo> =
                { Pair( it.rowid, TrustedDeviceInfo.fromJSON(it.params)) }

        val eventIdsToAcknowledge = events
                .filter(isDeviceLinkRequest)
                .map(toIdAndDeviceInfoPair)

        if (eventIdsToAcknowledge.isNotEmpty())
            acknowledgeEventsIgnoringErrors(eventIdsToAcknowledge.map { it.first })

        val deviceInfo = eventIdsToAcknowledge.map { it.second }
        if(deviceInfo.isEmpty())
            throw Exception()

        return eventIdsToAcknowledge.map { it.second }.last()
    }

    private fun acknowledgeEventsIgnoringErrors(eventIdsToAcknowledge: List<Long>): Boolean {
        try {
            if(eventIdsToAcknowledge.isNotEmpty())
                apiClient.acknowledgeEvents(eventIdsToAcknowledge)
        } catch (ex: IOException) {
            // if this request fails, just ignore it, we can acknowledge again later
        }
        return eventIdsToAcknowledge.isNotEmpty()
    }

    override fun cancel() {
    }
}
