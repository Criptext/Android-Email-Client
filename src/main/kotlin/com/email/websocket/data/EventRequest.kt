package com.email.websocket.data

import com.email.api.models.*

/**
 * Created by gabriel on 5/1/18.
 */
sealed class EventRequest {
    data class InsertNewEmail(val emailMetadata: EmailMetadata): EventRequest()
    data class UpdateDeliveryStatus(val trackingUpdate: TrackingUpdate): EventRequest()
    data class UpdatePeerUnsendEmailStatus(val eventId: Long, val peerUnsendEmailStatusUpdate: PeerUnsendEmailStatusUpdate): EventRequest()
    data class UpdatePeerReadEmailStatus(val eventId: Long, val peerReadEmailStatusUpdate: PeerReadEmailStatusUpdate): EventRequest()
    data class UpdatePeerReadThreadStatus(val eventId: Long, val peerReadThreadStatusUpdate: PeerReadThreadStatusUpdate): EventRequest()
    data class UpdatePeerEmailDeletedStatus(val eventId: Long, val peerEmailDeleted: PeerEmailDeletedStatusUpdate): EventRequest()
    data class UpdatePeerThreadDeletedStatus(val eventId: Long, val peerThreadDeleted: PeerThreadDeletedStatusUpdate): EventRequest()
    data class UpdatePeerEmailLabelsChangedStatus(val eventId: Long, val peerEmailLabelsChanged: PeerEmailLabelsChangedStatusUpdate): EventRequest()
    data class UpdatePeerThreadLabelsChangedStatus(val eventId: Long, val peerThreadsLabelChanged: PeerThreadLabelsChangedStatusUpdate): EventRequest()
    data class UpdatePeerUsernameChangedStatus(val eventId: Long, val peerUsernameChanged: PeerUsernameChangedStatusUpdate): EventRequest()
    data class UpdatePeerLabelCreatedStatus(val eventId: Long, val peerLabelCreated: PeerLabelCreatedStatusUpdate): EventRequest()
}