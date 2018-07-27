package com.email.websocket.data

import com.email.api.HttpClient
import com.email.api.models.PeerThreadDeletedStatusUpdate
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.dao.EmailDao
import com.email.db.models.ActiveAccount

class UpdatePeerThreadDeletedStatusWorker(private val eventId: Long,
                                          private val dao: EmailDao,
                                          httpClient: HttpClient,
                                          activeAccount: ActiveAccount,
                                          override val publishFn: (EventResult.UpdatePeerThreadDeletedStatus) -> Unit,
                                          private val peerThreadDeletedStatusUpdate: PeerThreadDeletedStatusUpdate
                       ) : BackgroundWorker<EventResult.UpdatePeerThreadDeletedStatus> {

    override val canBeParallelized = false
    private val apiClient = WebSocketAPIClient(httpClient, activeAccount.jwt)


    override fun catchException(ex: Exception): EventResult.UpdatePeerThreadDeletedStatus {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(reporter: ProgressReporter<EventResult.UpdatePeerThreadDeletedStatus>)
            : EventResult.UpdatePeerThreadDeletedStatus? {

        if(!peerThreadDeletedStatusUpdate.threadIds.isEmpty()){
            val update = ThreadDeletedPeerStatusUpdate(peerThreadDeletedStatusUpdate)
            dao.deleteThreads(peerThreadDeletedStatusUpdate.threadIds)
            apiClient.acknowledgeEvents(eventId)
            return EventResult.UpdatePeerThreadDeletedStatus.Success(update)
        }
        return EventResult.UpdatePeerThreadDeletedStatus.Success(null)
    }

    override fun cancel() {
        TODO("not implemented")
    }
}