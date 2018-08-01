package com.email.websocket.data

import com.email.api.HttpClient
import com.email.api.models.PeerLabelCreatedStatusUpdate
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.LabelTypes
import com.email.db.dao.LabelDao
import com.email.db.models.ActiveAccount
import com.email.db.models.Label
import com.email.utils.ColorUtils

class UpdatePeerLabelCreatedStatusWorker(private val eventId: Long,
                                         private val labelDao: LabelDao,
                                         httpClient: HttpClient,
                                         activeAccount: ActiveAccount,
                                         override val publishFn: (EventResult.UpdatePeerLabelCreatedStatus) -> Unit,
                                         private val peerLabelCreatedStatusUpdate: PeerLabelCreatedStatusUpdate
                       ) : BackgroundWorker<EventResult.UpdatePeerLabelCreatedStatus> {

    override val canBeParallelized = false
    private val apiClient = WebSocketAPIClient(httpClient, activeAccount.jwt)


    override fun catchException(ex: Exception): EventResult.UpdatePeerLabelCreatedStatus {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(reporter: ProgressReporter<EventResult.UpdatePeerLabelCreatedStatus>)
            : EventResult.UpdatePeerLabelCreatedStatus? {

        if(!labelDao.alreadyExists(peerLabelCreatedStatusUpdate.text)){
            val id = labelDao.insert(Label(
                    id = 0,
                    text = peerLabelCreatedStatusUpdate.text,
                    color = ColorUtils.colorStringByName(peerLabelCreatedStatusUpdate.color),
                    visible = true,
                    type = LabelTypes.CUSTOM
            ))
            if (id > 0) {
                val update = LabelCreatedPeerStatusUpdate(peerLabelCreatedStatusUpdate)
                apiClient.acknowledgeEvents(eventId)
                return EventResult.UpdatePeerLabelCreatedStatus.Success(update)
            }
        }
        apiClient.acknowledgeEvents(eventId)
        return EventResult.UpdatePeerLabelCreatedStatus.Success(null)
    }

    override fun cancel() {
        TODO("not implemented")
    }
}