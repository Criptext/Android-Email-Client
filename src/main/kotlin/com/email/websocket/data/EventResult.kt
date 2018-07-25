package com.email.websocket.data

import com.email.api.models.PeerEmailStatusUpdate
import com.email.db.models.Email
import com.email.utils.UIMessage

/**
 * Created by gabriel on 5/1/18.
 */
sealed class EventResult {
    sealed class InsertNewEmail: EventResult()  {
        data class Success(val newEmail: Email): InsertNewEmail()
        class Failure(val message: UIMessage): InsertNewEmail()
    }
    sealed class UpdateDeliveryStatus: EventResult()  {
        data class Success(val update: EmailDeliveryStatusUpdate?): UpdateDeliveryStatus()
        class Failure(val message: UIMessage): UpdateDeliveryStatus()
    }

    sealed class UpdatePeerEmailStatus: EventResult()  {
        data class Success(val update: EmailPeerStatusUpdate?): UpdatePeerEmailStatus()
        class Failure(val message: UIMessage): UpdatePeerEmailStatus()
    }

}