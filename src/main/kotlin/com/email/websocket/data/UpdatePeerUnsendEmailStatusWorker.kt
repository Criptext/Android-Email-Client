package com.email.websocket.data

import com.email.api.models.PeerEmailStatusUpdate
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.DeliveryTypes
import com.email.db.FeedType
import com.email.db.dao.ContactDao
import com.email.db.dao.EmailDao
import com.email.db.dao.FeedItemDao
import com.email.db.dao.FileDao
import com.email.db.models.Contact
import com.email.db.models.FeedItem
import com.email.utils.DateUtils
import java.util.*

class UpdatePeerUnsendEmailStatusWorker(private val dao: EmailDao,
                                        private val fileDao: FileDao,
                                        override val publishFn: (EventResult.UpdatePeerEmailStatus) -> Unit,
                                        private val peerEmailStatusUpdate: PeerEmailStatusUpdate)
    : BackgroundWorker<EventResult.UpdatePeerEmailStatus> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): EventResult.UpdatePeerEmailStatus {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(reporter: ProgressReporter<EventResult.UpdatePeerEmailStatus>)
            : EventResult.UpdatePeerEmailStatus? {
        val existingEmail = dao.findEmailByMetadataKey(peerEmailStatusUpdate.metadataKey)

        if (existingEmail != null) {

            dao.changeDeliveryTypeByMetadataKey(listOf(peerEmailStatusUpdate.metadataKey), DeliveryTypes.UNSEND)
            val update = EmailPeerStatusUpdate(existingEmail.id, peerEmailStatusUpdate)

            dao.changeDeliveryType(existingEmail.id, DeliveryTypes.UNSEND)
            dao.unsendEmailById(existingEmail.id, "", "",
                    DateUtils.parseDateWithServerFormat(peerEmailStatusUpdate.unsendDate, false))
            fileDao.changeFileStatusByEmailid(existingEmail.id, 0)

            return EventResult.UpdatePeerEmailStatus.Success(update)
        }

        // nothing was updated so return null.
        return EventResult.UpdatePeerEmailStatus.Success(null)
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}