package com.email.websocket.data

import com.email.api.HttpClient
import com.email.api.models.PeerEmailDeletedStatusUpdate
import com.email.api.models.PeerReadThreadStatusUpdate
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.dao.EmailDao
import com.email.db.models.ActiveAccount
import com.email.db.models.Email

class UpdatePeerEmailDeletedStatusWorker(private val eventId:Long,
                                         private val dao: EmailDao,
                                         httpClient: HttpClient,
                                         activeAccount: ActiveAccount,
                                         override val publishFn: (EventResult.UpdatePeerEmailDeletedStatus) -> Unit,
                                         private val peerEmailDeletedStatusUpdate: PeerEmailDeletedStatusUpdate
                       ) : BackgroundWorker<EventResult.UpdatePeerEmailDeletedStatus> {

    override val canBeParallelized = false
    private val apiClient = WebSocketAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): EventResult.UpdatePeerEmailDeletedStatus {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(reporter: ProgressReporter<EventResult.UpdatePeerEmailDeletedStatus>)
            : EventResult.UpdatePeerEmailDeletedStatus? {

        if(!peerEmailDeletedStatusUpdate.metadataKeys.isEmpty()){
            val update = EmailDeletedPeerStatusUpdate(dao.getAllEmailsByMetadataKey(peerEmailDeletedStatusUpdate.metadataKeys).map { it.id },
                    peerEmailDeletedStatusUpdate)
            dao.deleteAll(dao.getAllEmailsByMetadataKey(peerEmailDeletedStatusUpdate.metadataKeys))
            apiClient.acknowledgeEvents(eventId)
            return EventResult.UpdatePeerEmailDeletedStatus.Success(update)
        }
        return EventResult.UpdatePeerEmailDeletedStatus.Success(null)
    }

    override fun cancel() {
        TODO("not implemented")
    }
}