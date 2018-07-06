package com.email.websocket.data

import com.email.api.models.TrackingUpdate
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.DeliveryTypes
import com.email.db.FeedType
import com.email.db.dao.ContactDao
import com.email.db.dao.EmailDao
import com.email.db.dao.FeedItemDao
import com.email.db.models.Contact
import com.email.db.models.FeedItem
import java.util.*

/**
 * Created by gabriel on 6/29/18.
 */

class UpdateDeliveryStatusWorker(private val dao: EmailDao,
                                 private val feedDao: FeedItemDao,
                                 private val contactDao: ContactDao,
                                 override val publishFn: (EventResult.UpdateDeliveryStatus) -> Unit,
                                 private val trackingUpdate: TrackingUpdate)
    : BackgroundWorker<EventResult.UpdateDeliveryStatus> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): EventResult.UpdateDeliveryStatus {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(reporter: ProgressReporter<EventResult.UpdateDeliveryStatus>)
            : EventResult.UpdateDeliveryStatus? {
        val existingEmail = dao.findEmailByMetadataKey(trackingUpdate.metadataKey)

        if (existingEmail != null && existingEmail.delivered != DeliveryTypes.READ) {

            dao.changeDeliveryTypeByMetadataKey(listOf(trackingUpdate.metadataKey), DeliveryTypes.READ)
            val update = EmailDeliveryStatusUpdate(existingEmail.id, trackingUpdate)

            feedDao.insertFeedItem(FeedItem(
                    id = 0,
                    date = Date(),
                    feedType = FeedType.OPEN_EMAIL,
                    location = "",
                    seen = false,
                    emailId = existingEmail.id,
                    contactId = contactDao.getContact("${trackingUpdate.from}@${Contact.mainDomain}")!!.id,
                    fileId = null
            ))

            return EventResult.UpdateDeliveryStatus.Success(update)
        }

        // nothing was updated so return null.
        return EventResult.UpdateDeliveryStatus.Success(null)
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}