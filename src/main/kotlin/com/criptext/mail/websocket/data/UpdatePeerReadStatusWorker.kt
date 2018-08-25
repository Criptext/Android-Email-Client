package com.criptext.mail.websocket.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.PeerReadEmailStatusUpdate
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.dao.EmailDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Email

class UpdatePeerReadStatusWorker(private val eventId: Long,
                                 private val dao: EmailDao,
                                 httpClient: HttpClient,
                                 activeAccount: ActiveAccount,
                                 override val publishFn: (EventResult.UpdatePeerReadEmailStatus) -> Unit,
                                 private val peerReadEmailStatusUpdate: PeerReadEmailStatusUpdate
                       ) : BackgroundWorker<EventResult.UpdatePeerReadEmailStatus> {

    override val canBeParallelized = false
    private val apiClient = WebSocketAPIClient(httpClient, activeAccount.jwt)


    override fun catchException(ex: Exception): EventResult.UpdatePeerReadEmailStatus {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(reporter: ProgressReporter<EventResult.UpdatePeerReadEmailStatus>)
            : EventResult.UpdatePeerReadEmailStatus? {
            dao.toggleReadByMetadataKey(metadataKeys = peerReadEmailStatusUpdate.metadataKeys, unread = peerReadEmailStatusUpdate.unread)
            val update = ReadEmailPeerStatusUpdate(peerReadEmailStatusUpdate.metadataKeys, peerReadEmailStatusUpdate)
            apiClient.acknowledgeEvents(eventId)
            return EventResult.UpdatePeerReadEmailStatus.Success(update)
    }

    override fun cancel() {
        TODO("not implemented")
    }
}