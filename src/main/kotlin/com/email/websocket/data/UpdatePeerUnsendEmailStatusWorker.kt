package com.email.websocket.data

import com.email.api.HttpClient
import com.email.api.models.PeerUnsendEmailStatusUpdate
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.DeliveryTypes
import com.email.db.dao.EmailDao
import com.email.db.dao.FileDao
import com.email.db.models.ActiveAccount
import com.email.utils.DateUtils
import java.util.*

class UpdatePeerUnsendEmailStatusWorker(private val eventId:Long,
                                        private val dao: EmailDao,
                                        private val fileDao: FileDao,
                                        httpClient: HttpClient,
                                        activeAccount: ActiveAccount,
                                        override val publishFn: (EventResult.UpdatePeerUnsendEmailStatus) -> Unit,
                                        private val peerUnsendEmailStatusUpdate: PeerUnsendEmailStatusUpdate)
    : BackgroundWorker<EventResult.UpdatePeerUnsendEmailStatus> {

    override val canBeParallelized = false
    private val apiClient = WebSocketAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): EventResult.UpdatePeerUnsendEmailStatus {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(reporter: ProgressReporter<EventResult.UpdatePeerUnsendEmailStatus>)
            : EventResult.UpdatePeerUnsendEmailStatus? {
        val existingEmail = dao.findEmailByMetadataKey(peerUnsendEmailStatusUpdate.metadataKey)

        if (existingEmail != null) {

            dao.changeDeliveryTypeByMetadataKey(listOf(peerUnsendEmailStatusUpdate.metadataKey), DeliveryTypes.UNSEND)
            val update = UnsendEmailPeerStatusUpdate(existingEmail.id, peerUnsendEmailStatusUpdate)

            dao.changeDeliveryType(existingEmail.id, DeliveryTypes.UNSEND)
            dao.unsendEmailById(existingEmail.id, "", "",
                    Date())
            fileDao.changeFileStatusByEmailid(existingEmail.id, 0)
            apiClient.acknowledgeEvents(eventId)

            return EventResult.UpdatePeerUnsendEmailStatus.Success(update)
        }

        // nothing was updated so return null.
        return EventResult.UpdatePeerUnsendEmailStatus.Success(null)
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}