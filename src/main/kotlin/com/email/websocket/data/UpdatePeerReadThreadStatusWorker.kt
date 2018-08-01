package com.email.websocket.data

import com.email.api.HttpClient
import com.email.api.models.PeerReadThreadStatusUpdate
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.dao.EmailDao
import com.email.db.models.ActiveAccount
import com.email.db.models.Email

class UpdatePeerReadThreadStatusWorker(private val eventId: Long,
                                       private val dao: EmailDao,
                                       httpClient: HttpClient,
                                       activeAccount: ActiveAccount,
                                       override val publishFn: (EventResult.UpdatePeerReadThreadStatus) -> Unit,
                                       private val peerReadThreadStatusUpdate: PeerReadThreadStatusUpdate
                       ) : BackgroundWorker<EventResult.UpdatePeerReadThreadStatus> {

    override val canBeParallelized = false
    private val apiClient = WebSocketAPIClient(httpClient, activeAccount.jwt)


    override fun catchException(ex: Exception): EventResult.UpdatePeerReadThreadStatus {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(reporter: ProgressReporter<EventResult.UpdatePeerReadThreadStatus>)
            : EventResult.UpdatePeerReadThreadStatus? {

        if(!peerReadThreadStatusUpdate.threadIds.isEmpty()){
            dao.toggleReadByThreadId(peerReadThreadStatusUpdate.threadIds, peerReadThreadStatusUpdate.unread)
            val update = ReadThreadPeerStatusUpdate(peerReadThreadStatusUpdate)
            apiClient.acknowledgeEvents(eventId)
            return EventResult.UpdatePeerReadThreadStatus.Success(update)
        }
        return EventResult.UpdatePeerReadThreadStatus.Success(null)
    }

    override fun cancel() {
        TODO("not implemented")
    }
}