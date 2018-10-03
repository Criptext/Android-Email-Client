package com.criptext.mail.websocket

import android.util.Log
import com.neovisionaries.ws.client.*

/**
 * WebSocketClient implementation using dependency nv-websocket-client
 * Created by gabriel on 9/15/17.
 */

class NVWebSocketClient: WebSocketClient {
    private var ws: WebSocket? = null
    private var onMessageReceived: ((String) -> Unit?)? = null

    private val webSocketAdapter = object : WebSocketAdapter() {
        override fun onDisconnected(websocket: WebSocket?, serverCloseFrame: WebSocketFrame?,
                                    clientCloseFrame: WebSocketFrame?, closedByServer: Boolean) {
            Log.e("WEBSOCKET", "DISCONNECTED!!!!")
            reconnect()
        }

        override fun handleCallbackError(websocket: WebSocket?, cause: Throwable?) {
            cause?.printStackTrace()
        }

        override fun onConnected(websocket: WebSocket?, headers: MutableMap<String,
                MutableList<String>>?) {
            Log.e("WEBSOCKET", "CONNECTED!!!!")
        }

        override fun onConnectError(websocket: WebSocket?, exception: WebSocketException?) {
            Log.e("WEBSOCKET", "Error : " + exception.toString())
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

