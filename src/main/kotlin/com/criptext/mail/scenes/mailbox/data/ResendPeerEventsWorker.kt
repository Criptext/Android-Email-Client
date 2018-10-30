package com.criptext.mail.scenes.mailbox.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.PeerEventsApiHandler
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.dao.PendingEventDao
import com.criptext.mail.db.models.ActiveAccount
import com.github.kittinunf.result.Result

class ResendPeerEventsWorker(
        private val pendingDao: PendingEventDao,
        httpClient: HttpClient,
        activeAccount: ActiveAccount,
        override val publishFn: (MailboxResult.ResendPeerEvents) -> Unit)
    : BackgroundWorker<MailboxResult.ResendPeerEvents> {

    private val peerEventsApiHandler = PeerEventsApiHandler.Default(httpClient, activeAccount.jwt, pendingDao)

    override val canBeParallelized = true

    override fun catchException(ex: Exception): MailboxResult.ResendPeerEvents {
        return MailboxResult.ResendPeerEvents.Failure(false)
    }

    override fun work(reporter: ProgressReporter<MailboxResult.ResendPeerEvents>): MailboxResult.ResendPeerEvents? {

        val result = peerEventsApiHandler.dispatchEvents()
        return when(result){
            is Result.Success -> {
                MailboxResult.ResendPeerEvents.Success(peerEventsApiHandler.checkForMorePendingEvents())
            }
            is Result.Failure -> {
                MailboxResult.ResendPeerEvents.Failure(peerEventsApiHandler.checkForMorePendingEvents())
            }
        }
    }

    override fun cancel() {
    }

}