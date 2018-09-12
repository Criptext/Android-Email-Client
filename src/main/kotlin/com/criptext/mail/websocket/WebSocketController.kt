package com.criptext.mail.websocket

import com.criptext.mail.R
import com.criptext.mail.api.Hosts
import com.criptext.mail.api.models.Event
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.utils.UIMessage

/**
 * Manages the web socket, exposes methods to connect, disconnect, reconnect and subscribe/unsubscribe
 * listeners to the web socket. Parses the text messages from commands received via the socket and
 * publishes events to subscriber scene controllers.
 * Created by gabriel on 9/15/17.
 */

class WebSocketController(private val wsClient: WebSocketClient, activeAccount: ActiveAccount): WebSocketEventPublisher {

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
            Event.Cmd.newEmail,
            Event.Cmd.trackingUpdate,
            Event.Cmd.peerEmailReadStatusUpdate,
            Event.Cmd.peerEmailThreadReadStatusUpdate,
            Event.Cmd.peerEmailChangedLabels,
            Event.Cmd.peerThreadChangedLabels,
            Event.Cmd.peerEmailDeleted,
            Event.Cmd.peerThreadDeleted,
            Event.Cmd.peerEmailUnsendStatusUpdate,
            Event.Cmd.peerLabelCreated,
            Event.Cmd.recoveryEmailChanged,
            Event.Cmd.recoveryEmailConfirmed,
            Event.Cmd.peerUserChangeName -> currentListener?.onNewEvent()
            Event.Cmd.deviceRemoved -> currentListener?.onDeviceRemoved()
            Event.Cmd.deviceLock -> currentListener?.onDeviceLocked()

            else -> currentListener?.onError(UIMessage(R.string.web_socket_error,
                    arrayOf(event.cmd)))
        }
    }

    init {
        val url = createCriptextSocketServerURL(activeAccount.jwt)
        wsClient.connect(url, onMessageReceived)
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
