package com.criptext.mail.scenes.mailbox.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.PeerEventsApiHandler
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.PendingEventDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.mailbox.data.MailboxResult
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result

class ResendPeerEventsWorker(
        pendingDao: PendingEventDao,
        activeAccount: ActiveAccount,
        storage: KeyValueStorage,
        accountDao: AccountDao,
        override val publishFn: (MailboxResult.ResendPeerEvents) -> Unit)
    : BackgroundWorker<MailboxResult.ResendPeerEvents> {

    private val peerEventsApiHandler = PeerEventsApiHandler.Default(activeAccount, pendingDao,
            storage, accountDao)

    override val canBeParallelized = true

    override fun catchException(ex: Exception): MailboxResult.ResendPeerEvents {
        if(ex is ServerErrorException)
            return MailboxResult.ResendPeerEvents.ServerFailure(UIMessage(R.string.server_bad_status, arrayOf(ex.errorCode)),peerEventsApiHandler.checkForMorePendingEvents())
        return MailboxResult.ResendPeerEvents.Failure(peerEventsApiHandler.checkForMorePendingEvents())
    }

    override fun work(reporter: ProgressReporter<MailboxResult.ResendPeerEvents>): MailboxResult.ResendPeerEvents? {

        val result = peerEventsApiHandler.dispatchEvents()
        return when(result){
            is Result.Success -> {
                MailboxResult.ResendPeerEvents.Success(peerEventsApiHandler.checkForMorePendingEvents())
            }
            is Result.Failure -> {
                catchException(result.error)
            }
        }
    }

    override fun cancel() {
    }

}