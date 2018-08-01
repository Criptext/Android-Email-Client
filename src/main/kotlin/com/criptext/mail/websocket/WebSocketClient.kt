package com.criptext.mail.websocket

/**
 * Interface for web socket client implementations.
 * Created by gabriel on 9/15/17.
 */

interface WebSocketClient {
    fun disconnect()

    fun reconnect()

    fun connect(url: String, onMessageReceived: (String) -> Unit)
}
