package com.criptext.mail.scenes.mailbox.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.Event
import com.criptext.mail.api.models.UntrustedDeviceInfo
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.utils.EventLoader
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import org.json.JSONArray
import java.io.IOException

class GetPendingLinkRequestWorker(
        httpClient: HttpClient,
        activeAccount: ActiveAccount,
        override val publishFn: (
                MailboxResult.GetPendingLinkRequest) -> Unit)
    : BackgroundWorker<MailboxResult.GetPendingLinkRequest> {

    private val apiClient = MailboxAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.GetPendingLinkRequest =
             MailboxResult.GetPendingLinkRequest.Failure()

    override fun work(reporter: ProgressReporter<MailboxResult.GetPendingLinkRequest>)
            : MailboxResult.GetPendingLinkRequest? {
        val result = EventLoader.getEvents(apiClient)
                .flatMap(processEvents)
        return when (result) {
            is Result.Success -> {
                MailboxResult.GetPendingLinkRequest.Success(result.value)
            }
            is Result.Failure -> {
                catchException(result.error)
            }
        }
    }



    private val processEvents: (List<Event>) -> Result<UntrustedDeviceInfo, Exception> = { events ->
        Result.of {
            processLinkRequestEvents(events)
        }
    }

    private fun processLinkRequestEvents(events: List<Event>): UntrustedDeviceInfo {
        val isDeviceLinkRequest: (Event) -> Boolean = { it.cmd == Event.Cmd.deviceAuthRequest }
        val toIdAndDeviceInfoPair: (Event) -> Pair<Long, UntrustedDeviceInfo> =
                { Pair( it.rowid, UntrustedDeviceInfo.fromJSON(it.params)) }

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
