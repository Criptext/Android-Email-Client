package com.criptext.mail.temporalwebsocket

import android.util.Log
import com.criptext.mail.websocket.WebSocketClient
import com.neovisionaries.ws.client.*

/**
 * WebSocketClient implementation using dependency nv-websocket-client
 */

class TempWebSocketClient: WebSocketClient {
    private var ws: WebSocket? = null
    private var onMessageReceived: ((String) -> Unit?)? = null

    private val webSocketAdapter = object : WebSocketAdapter() {
        override fun onDisconnected(websocket: WebSocket?, serverCloseFrame: WebSocketFrame?,
                                    clientCloseFrame: WebSocketFrame?, closedByServer: Boolean) {
            reconnect()
            Log.e("TEMP-WEBSOCKET", "DISCONNECTED")
        }

        override fun handleCallbackError(websocket: WebSocket?, cause: Throwable?) {
            cause?.printStackTrace()
        }

        override fun onConnected(websocket: WebSocket?, headers: MutableMap<String,
                MutableList<String>>?) {
            Log.e("TEMP-WEBSOCKET", "CONNECTED")
        }

        override fun onConnectError(websocket: WebSocket?, exception: WebSocketException?) {
            Log.e("TEMP-WEBSOCKET", "Error : " + exception.toString())
        }

        override fun onTextMessage(websocket: WebSocket, text: String) {
            onMessageReceived?.invoke(text)
        }
    }

    override fun disconnect() {
        ws?.disconnect()
    }

    override fun reconnect() {
        if (ws != null && ws!!.state == WebSocketState.CLOSED) {
            ws = ws!!.recreate()
            ws!!.connectAsynchronously()
        }
    }

    override fun connect(url: String, onMessageReceived: (String) -> Unit?) {
        this.onMessageReceived = onMessageReceived
        val ws = WebSocketFactory().createSocket(url)
        ws.addProtocol("criptext-protocol")
        ws.addListener(webSocketAdapter)
        ws.connectAsynchronously()
        this.ws = ws

    }

}

