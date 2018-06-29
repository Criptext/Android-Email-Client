package com.email.websocket

import com.email.api.Hosts
import com.email.api.models.EmailMetadata
import com.email.api.models.Event
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

    override var listener: WebSocketEventListener? = null

    private val onMessageReceived = { text: String ->
        val event = Event.fromJSON(text)
        if (event.cmd == Event.Cmd.newEmail) {
            val emailMetadata = EmailMetadata.fromJSON(event.params)
            eventDataSource.submitRequest(EventRequest.InsertNewEmail(emailMetadata))
        }
    }

    private val dataSourceListener = { eventResult: EventResult ->
        when (eventResult) {
            is EventResult.InsertNewEmail -> publishNewEmailResult(eventResult)
        }
    }

    init {
        val url = createCriptextSocketServerURL(
                recipientId = activeAccount.recipientId,
                deviceId = activeAccount.deviceId)

        eventDataSource.listener = dataSourceListener
        wsClient.connect(url, onMessageReceived)
    }

    private fun publishNewEmailResult(eventResult: EventResult.InsertNewEmail) {
        when (eventResult) {
            is EventResult.InsertNewEmail.Success -> listener?.onNewEmail(eventResult.newEmail)
            is EventResult.InsertNewEmail.Failure -> listener?.onError(eventResult.message)
        }
    }

    fun disconnect() {
        wsClient.disconnect()
    }

    fun reconnect() {
        wsClient.reconnect()
    }

    companion object {
        private fun createCriptextSocketServerURL(recipientId: String, deviceId: Int): String {
            return """${Hosts.webSocketBaseUrl}?recipientId=$recipientId&deviceId=$deviceId"""
        }
    }

}
