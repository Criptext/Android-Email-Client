package com.email.websocket.data

import com.email.api.models.EmailMetadata
import com.email.api.models.PeerEmailStatusUpdate
import com.email.api.models.TrackingUpdate

/**
 * Created by gabriel on 5/1/18.
 */
sealed class EventRequest {
    data class InsertNewEmail(val emailMetadata: EmailMetadata): EventRequest()
    data class UpdateDeliveryStatus(val trackingUpdate: TrackingUpdate): EventRequest()
    data class UpdatePeerEmailStatus(val peerEmailStatusUpdate: PeerEmailStatusUpdate): EventRequest()
}