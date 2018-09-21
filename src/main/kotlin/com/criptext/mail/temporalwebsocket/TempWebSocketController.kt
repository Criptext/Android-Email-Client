package com.criptext.mail.temporalwebsocket

import com.criptext.mail.R
import com.criptext.mail.api.Hosts
import com.criptext.mail.api.models.Event
import com.criptext.mail.api.models.UntrustedDeviceInfo
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.websocket.WebSocketClient
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher
import org.json.JSONObject

/**
 * Manages the web socket, exposes methods to connect, disconnect, reconnect and subscribe/unsubscribe
 * listeners to the web socket. Parses the text messages from commands received via the socket and
 * publishes events to subscriber scene controllers.
 * Created by gabriel on 9/15/17.
 */

class TempWebSocketController(private val wsClient: TempWebSocketClient,
                              jwt: String): WebSocketEventPublisher {

    private var currentListener: WebSocketEventListener? = null

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
            Event.Cmd.deviceAuthConfirmed -> {
                val deviceId = JSONObject(event.params).getInt("deviceId")
                val name = JSONObject(event.params).getString("name")
                currentListener?.onDeviceLinkAuthAccept(deviceId, name)
            }
            Event.Cmd.deviceAuthDenied -> currentListener?.onDeviceLinkAuthDeny()

            else -> currentListener?.onError(UIMessage(R.string.web_socket_error,
                    arrayOf(event.cmd)))
        }
    }

    init {
        val url = createCriptextSocketServerURL(jwt)
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
