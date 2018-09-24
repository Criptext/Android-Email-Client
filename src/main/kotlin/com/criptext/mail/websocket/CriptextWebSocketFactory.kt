package com.criptext.mail.websocket

import com.criptext.mail.temporalwebsocket.TempWebSocketClient
import com.criptext.mail.temporalwebsocket.TempWebSocketController

class CriptextWebSocketFactory {

    fun createWebSocket(jwt: String): WebSocketEventPublisher{
        return WebSocketController(NVWebSocketClient(), jwt)
    }

    fun createTemporalWebSocket(jwt: String): WebSocketEventPublisher{
        return TempWebSocketController(TempWebSocketClient(), jwt)
    }
}