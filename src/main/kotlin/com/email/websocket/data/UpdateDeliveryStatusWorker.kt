package com.email.websocket.data

import com.email.api.models.TrackingUpdate
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.DeliveryTypes
import com.email.db.dao.EmailDao

/**
 * Created by gabriel on 6/29/18.
 */

class UpdateDeliveryStatusWorker(private val dao: EmailDao,
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

        if (existingEmail?.delivered != DeliveryTypes.READ) {
            dao.changeDeliveryTypeByMetadataKey(listOf(trackingUpdate.metadataKey), DeliveryTypes.READ)
            return EventResult.UpdateDeliveryStatus.Success(trackingUpdate)
        }

        // nothing was updated so return null.
        return EventResult.UpdateDeliveryStatus.Success(null)
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}