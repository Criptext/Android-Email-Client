package com.email.websocket.data

import com.email.api.HttpClient
import com.email.api.models.PeerUsernameChangedStatusUpdate
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.dao.*
import com.email.db.models.ActiveAccount
import com.email.db.models.Contact

class UpdatePeerUsernameChangedStatusWorker(private val eventId: Long,
                                            private val contactDao: ContactDao,
                                            private val accountDao: AccountDao,
                                            httpClient: HttpClient,
                                            private val activeAccount: ActiveAccount,
                                            override val publishFn: (EventResult.UpdatePeerUsernameChangedStatus) -> Unit,
                                            private val peerUsernameChangedStatusUpdate: PeerUsernameChangedStatusUpdate
                       ) : BackgroundWorker<EventResult.UpdatePeerUsernameChangedStatus> {

    override val canBeParallelized = false
    private val apiClient = WebSocketAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): EventResult.UpdatePeerUsernameChangedStatus {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(reporter: ProgressReporter<EventResult.UpdatePeerUsernameChangedStatus>)
            : EventResult.UpdatePeerUsernameChangedStatus? {

        contactDao.updateContactName("${activeAccount.recipientId}@${Contact.mainDomain}", peerUsernameChangedStatusUpdate.name)
        accountDao.updateProfileName(peerUsernameChangedStatusUpdate.name, activeAccount.recipientId)
        val update = UsernameChangedPeerStatusUpdate(peerUsernameChangedStatusUpdate)
        apiClient.acknowledgeEvents(eventId)
        return EventResult.UpdatePeerUsernameChangedStatus.Success(update)
    }

    override fun cancel() {
        TODO("not implemented")
    }
}