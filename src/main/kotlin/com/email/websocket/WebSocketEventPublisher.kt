package com.email.websocket

/**
 * Objects that want to listen to events emitted by the web socket but are not allowed to manipulate
 * the socket's connection should interact with the socket exclusively via this interface.
 * Created by gabriel on 9/15/17.
 */

interface WebSocketEventPublisher {
    var listener: WebSocketEventListener?
}
