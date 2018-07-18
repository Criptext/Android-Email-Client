package com.email.websocket

import com.email.api.Hosts
import com.email.api.models.EmailMetadata
import com.email.api.models.Event
import com.email.api.models.TrackingUpdate
import com.email.db.models.ActiveAccount
import com.email.db.models.CRFile
import com.email.websocket.data.EventDataSource
import com.email.websocket.data.EventRequest
import com.email.websocket.data.EventResult

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
        }
    }

    private val dataSourceListener = { eventResult: EventResult ->
        when (eventResult) {
            is EventResult.InsertNewEmail -> publishNewEmailResult(eventResult)
            is EventResult.UpdateDeliveryStatus -> publishNewTrackingUpdate(eventResult)
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
