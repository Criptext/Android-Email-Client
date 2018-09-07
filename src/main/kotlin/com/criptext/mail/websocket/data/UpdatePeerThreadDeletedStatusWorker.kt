package com.criptext.mail.websocket.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.PeerThreadDeletedStatusUpdate
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.dao.EmailDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label

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
            val emailsToDelete = dao.getEmailsFromThreadIdByLabel(Label.defaultItems.trash.id,
                    peerThreadDeletedStatusUpdate.threadIds)
            dao.deleteAll(emailsToDelete)
            apiClient.acknowledgeEvents(eventId)
            return EventResult.UpdatePeerThreadDeletedStatus.Success(update)
        }
        return EventResult.UpdatePeerThreadDeletedStatus.Success(null)
    }

    override fun cancel() {
        TODO("not implemented")
    }
}