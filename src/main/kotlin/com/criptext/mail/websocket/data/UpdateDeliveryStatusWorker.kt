package com.criptext.mail.websocket.data

import com.criptext.mail.api.models.TrackingUpdate
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.FeedType
import com.criptext.mail.db.dao.ContactDao
import com.criptext.mail.db.dao.EmailDao
import com.criptext.mail.db.dao.FeedItemDao
import com.criptext.mail.db.dao.FileDao
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.FeedItem
import com.criptext.mail.db.typeConverters.EmailDeliveryConverter
import com.criptext.mail.utils.DateUtils
import java.io.File
import java.util.*

/**
 * Created by gabriel on 6/29/18.
 */

class UpdateDeliveryStatusWorker(private val dao: EmailDao,
                                 private val feedDao: FeedItemDao,
                                 private val fileDao: FileDao,
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

        if (existingEmail != null) {

            dao.changeDeliveryTypeByMetadataKey(listOf(trackingUpdate.metadataKey), trackingUpdate.type, DeliveryTypes.UNSEND)
            val update = EmailDeliveryStatusUpdate(existingEmail.id, trackingUpdate)

            if(trackingUpdate.type == DeliveryTypes.READ) {
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
            }

            if(trackingUpdate.type == DeliveryTypes.UNSEND) {
                dao.changeDeliveryType(existingEmail.id, DeliveryTypes.UNSEND)
                dao.unsendEmailById(existingEmail.id, "", "",
                        DateUtils.getDateFromString(DateUtils.printDateWithServerFormat(Date()), "yyyy-MM-dd HH:mm:ss"))
                fileDao.changeFileStatusByEmailid(existingEmail.id, 0)
            }

            return EventResult.UpdateDeliveryStatus.Success(update)
        }

        // nothing was updated so return null.
        return EventResult.UpdateDeliveryStatus.Success(null)
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}