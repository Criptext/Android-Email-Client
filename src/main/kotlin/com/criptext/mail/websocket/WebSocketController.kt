package com.criptext.mail.websocket

import com.criptext.mail.api.Hosts
import com.criptext.mail.api.models.*
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.websocket.data.EventDataSource
import com.criptext.mail.websocket.data.EventRequest
import com.criptext.mail.websocket.data.EventResult

/**
 * Manages the web socket, exposes methods to connect, disconnect, reconnect and subscribe/unsubscribe
 * listeners to the web socket. Parses the text messages from commands received via the socket and
 * publishes events to subscriber scene controllers.
 * Created by gabriel on 9/15/17.
 */

class WebSocketController(private val wsClient: WebSocketClient, activeAccount: ActiveAccount,
                          private val eventDataSource: EventDataSource): WebSocketEventPublisher {

    var currentListener: WebSocketEventListener? = null

    override fun setListener(listener: WebSocketEventListener) {
        this.currentListener = listener
    }

    override fun clearListener(listener: WebSocketEventListener) {
        if (this.currentListener === listener)
            this.currentListener = null
    }

    private val onMessageReceived = { text: String ->
        val event = Event.fromJSON(text)
        when (event.cmd) {
            Event.Cmd.newEmail -> {
                val emailMetadata = EmailMetadata.fromJSON(event.params)
                eventDataSource.submitRequest(EventRequest.InsertNewEmail(emailMetadata))
            }
            Event.Cmd.trackingUpdate -> {
                val trackingUpdate = TrackingUpdate.fromJSON(event.params)
                if(trackingUpdate.from != activeAccount.recipientId) {
                    eventDataSource.submitRequest(EventRequest.UpdateDeliveryStatus(trackingUpdate))
                }
            }
            Event.Cmd.deviceRemoved -> {
                eventDataSource.submitRequest(EventRequest.DeviceRemoved())
            }
            Event.Cmd.peerEmailReadStatusUpdate -> {
                val peerReadEmailStatusUpdate = PeerReadEmailStatusUpdate.fromJSON(event.params)
                eventDataSource.submitRequest(EventRequest.UpdatePeerReadEmailStatus(event.rowid,
                        peerReadEmailStatusUpdate))
            }
            Event.Cmd.peerEmailThreadReadStatusUpdate -> {
                val peerReadThreadStatusUpdate = PeerReadThreadStatusUpdate.fromJSON(event.params)
                eventDataSource.submitRequest(EventRequest.UpdatePeerReadThreadStatus(event.rowid,
                        peerReadThreadStatusUpdate))
            }
            Event.Cmd.peerEmailChangedLabels -> {
                val peerEmailLabelChanged = PeerEmailLabelsChangedStatusUpdate.fromJSON(event.params)
                eventDataSource.submitRequest(EventRequest.UpdatePeerEmailLabelsChangedStatus(event.rowid,
                        peerEmailLabelChanged))
            }
            Event.Cmd.peerThreadChangedLabels -> {
                val peerThreadLabelChanged = PeerThreadLabelsChangedStatusUpdate.fromJSON(event.params)
                eventDataSource.submitRequest(EventRequest.UpdatePeerThreadLabelsChangedStatus(event.rowid,
                        peerThreadLabelChanged))
            }
            Event.Cmd.peerEmailDeleted -> {
                val peerEmailDeleted = PeerEmailDeletedStatusUpdate.fromJSON(event.params)
                eventDataSource.submitRequest(EventRequest.UpdatePeerEmailDeletedStatus(event.rowid,
                        peerEmailDeleted))
            }
            Event.Cmd.peerThreadDeleted -> {
                val peerThreadDeleted = PeerThreadDeletedStatusUpdate.fromJSON(event.params)
                eventDataSource.submitRequest(EventRequest.UpdatePeerThreadDeletedStatus(event.rowid,
                        peerThreadDeleted))
            }
            Event.Cmd.peerEmailUnsendStatusUpdate -> {
                val peerUnsendEmailStatusUpdate = PeerUnsendEmailStatusUpdate.fromJSON(event.params)
                eventDataSource.submitRequest(EventRequest.UpdatePeerUnsendEmailStatus(event.rowid,
                        peerUnsendEmailStatusUpdate))
            }
            Event.Cmd.peerLabelCreated -> {
                val peerLabelCreatedStatusUpdate = PeerLabelCreatedStatusUpdate.fromJSON(event.params)
                eventDataSource.submitRequest(EventRequest.UpdatePeerLabelCreatedStatus(event.rowid,
                        peerLabelCreatedStatusUpdate))
            }
            Event.Cmd.peerUserChangeName -> {
                val peerUsernameChangeStatusUpdate = PeerUsernameChangedStatusUpdate.fromJSON(event.params)
                eventDataSource.submitRequest(EventRequest.UpdatePeerUsernameChangedStatus(event.rowid,
                        peerUsernameChangeStatusUpdate))
            }
        }
    }

    private val dataSourceListener = { eventResult: EventResult ->
        when (eventResult) {
            is EventResult.InsertNewEmail -> publishNewEmailResult(eventResult)
            is EventResult.UpdateDeliveryStatus -> publishNewTrackingUpdate(eventResult)
            is EventResult.DeviceRemoved -> onDeviceRemoved(eventResult)
            //PEER EVENTS
            is EventResult.UpdatePeerReadEmailStatus -> publishNewReadEmail(eventResult)
            is EventResult.UpdatePeerReadThreadStatus -> publishNewReadThread(eventResult)
            is EventResult.UpdatePeerUnsendEmailStatus -> publishNewUnsendEmail(eventResult)
            is EventResult.UpdatePeerEmailDeletedStatus -> publishNewEmailDeleted(eventResult)
            is EventResult.UpdatePeerThreadDeletedStatus -> publishNewThreadDeleted(eventResult)
            is EventResult.UpdatePeerEmailChangedLabelsStatus -> publishNewEmailLabelsChanged(eventResult)
            is EventResult.UpdatePeerThreadChangedLabelsStatus -> publishNewThreadLabelsChanged(eventResult)
            is EventResult.UpdatePeerUsernameChangedStatus -> publishNewUserNameChanged(eventResult)
            is EventResult.UpdatePeerLabelCreatedStatus -> publishNewLabelCreated(eventResult)
        }
    }

    init {
        val url = createCriptextSocketServerURL(activeAccount.jwt)

        eventDataSource.listener = dataSourceListener
        wsClient.connect(url, onMessageReceived)
    }

    private fun publishNewEmailResult(eventResult: EventResult.InsertNewEmail) {
        when (eventResult) {
            is EventResult.InsertNewEmail.Success -> currentListener?.onNewEmail(eventResult.newEmail)
            is EventResult.InsertNewEmail.Failure -> currentListener?.onError(eventResult.message)
        }
    }

    private fun publishNewTrackingUpdate(eventResult: EventResult.UpdateDeliveryStatus) {

        when (eventResult) {
            is EventResult.UpdateDeliveryStatus.Success -> {
                val update = eventResult.update
                if (update != null)
                    currentListener?.onNewTrackingUpdate(update.emailId, update.trackingUpdate)
            }

            is EventResult.UpdateDeliveryStatus.Failure ->
                currentListener?.onError(eventResult.message)
        }
    }

    private fun onDeviceRemoved(eventResult: EventResult.DeviceRemoved) {

        when (eventResult) {
            is EventResult.DeviceRemoved.Success -> {
                currentListener?.onDeviceRemoved()
            }

            is EventResult.DeviceRemoved.Failure ->{
                //DoNothig?
            }
        }
    }

    private fun publishNewEmailLabelsChanged(eventResult: EventResult.UpdatePeerEmailChangedLabelsStatus) {

        when (eventResult) {
            is EventResult.UpdatePeerEmailChangedLabelsStatus.Success -> {
                val update = eventResult.update
                if (update != null)
                    currentListener?.onNewPeerEmailLabelsChangedUpdate(update.peerEmailLabelsChangedStatusUpdate)
            }

            is EventResult.UpdatePeerEmailChangedLabelsStatus.Failure ->
                currentListener?.onError(eventResult.message)
        }
    }

    private fun publishNewThreadLabelsChanged(eventResult: EventResult.UpdatePeerThreadChangedLabelsStatus) {

        when (eventResult) {
            is EventResult.UpdatePeerThreadChangedLabelsStatus.Success -> {
                val update = eventResult.update
                if (update != null)
                    currentListener?.onNewPeerThreadLabelsChangedUpdate(update.peerThreadLabelsChangedStatusUpdate)
            }

            is EventResult.UpdatePeerThreadChangedLabelsStatus.Failure ->
                currentListener?.onError(eventResult.message)
        }
    }

    private fun publishNewEmailDeleted(eventResult: EventResult.UpdatePeerEmailDeletedStatus) {

        when (eventResult) {
            is EventResult.UpdatePeerEmailDeletedStatus.Success -> {
                val update = eventResult.update
                if (update != null)
                    currentListener?.onNewPeerEmailDeletedUpdate(update.emailIds, update.peerEmailDeletedStatusUpdate)
            }

            is EventResult.UpdatePeerEmailDeletedStatus.Failure ->
                currentListener?.onError(eventResult.message)
        }
    }

    private fun publishNewThreadDeleted(eventResult: EventResult.UpdatePeerThreadDeletedStatus) {

        when (eventResult) {
            is EventResult.UpdatePeerThreadDeletedStatus.Success -> {
                val update = eventResult.update
                if (update != null)
                    currentListener?.onNewPeerThreadDeletedUpdate(update.peerThreadDeletedStatusUpdate)
            }

            is EventResult.UpdatePeerThreadDeletedStatus.Failure ->
                currentListener?.onError(eventResult.message)
        }
    }

    private fun publishNewReadEmail(eventResult: EventResult.UpdatePeerReadEmailStatus) {

        when (eventResult) {
            is EventResult.UpdatePeerReadEmailStatus.Success -> {
                val update = eventResult.update
                if (update != null)
                    currentListener?.onNewPeerReadEmailUpdate(update.matadataKeys, update.peerReadEmailStatusUpdate)
            }

            is EventResult.UpdatePeerReadEmailStatus.Failure ->
                currentListener?.onError(eventResult.message)
        }
    }

    private fun publishNewUnsendEmail(eventResult: EventResult.UpdatePeerUnsendEmailStatus) {

        when (eventResult) {
            is EventResult.UpdatePeerUnsendEmailStatus.Success -> {
                val update = eventResult.update
                if (update != null)
                    currentListener?.onNewPeerUnsendEmailUpdate(update.emailId, update.peerUnsendEmailStatusUpdate)
            }

            is EventResult.UpdatePeerUnsendEmailStatus.Failure ->
                currentListener?.onError(eventResult.message)
        }
    }

    private fun publishNewReadThread(eventResult: EventResult.UpdatePeerReadThreadStatus) {

        when (eventResult) {
            is EventResult.UpdatePeerReadThreadStatus.Success -> {
                val update = eventResult.update
                if (update != null)
                    currentListener?.onNewPeerReadThreadUpdate(update.peerReadThreadStatusUpdate)
            }

            is EventResult.UpdatePeerReadThreadStatus.Failure ->
                currentListener?.onError(eventResult.message)
        }
    }

    private fun publishNewUserNameChanged(eventResult: EventResult.UpdatePeerUsernameChangedStatus) {

        when (eventResult) {
            is EventResult.UpdatePeerUsernameChangedStatus.Success -> {
                val update = eventResult.update
                if (update != null)
                    currentListener?.onNewPeerUsernameChangedUpdate(update.peerUsernameChangedStatusUpdate)
            }

            is EventResult.UpdatePeerUsernameChangedStatus.Failure ->
                currentListener?.onError(eventResult.message)
        }
    }

    private fun publishNewLabelCreated(eventResult: EventResult.UpdatePeerLabelCreatedStatus) {

        when (eventResult) {
            is EventResult.UpdatePeerLabelCreatedStatus.Success -> {
                val update = eventResult.update
                if (update != null)
                    currentListener?.onNewPeerLabelCreatedUpdate(update.peerLabelCreatedStatusUpdate)
            }

            is EventResult.UpdatePeerLabelCreatedStatus.Failure ->
                currentListener?.onError(eventResult.message)
        }
    }

    fun disconnect() {
        wsClient.disconnect()
    }

    fun reconnect() {
        wsClient.reconnect()
    }

    companion object {
        private fun createCriptextSocketServerURL(jwt: String): String {
            return """${Hosts.webSocketBaseUrl}?token=$jwt"""
        }
    }

}
