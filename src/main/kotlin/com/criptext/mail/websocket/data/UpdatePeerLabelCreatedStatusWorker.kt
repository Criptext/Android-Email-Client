package com.criptext.mail.websocket.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.PeerLabelCreatedStatusUpdate
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.LabelTypes
import com.criptext.mail.db.dao.LabelDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.utils.ColorUtils

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