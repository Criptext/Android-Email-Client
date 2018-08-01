package com.criptext.mail.websocket

/**
 * Objects that want to listen to events emitted by the web socket but are not allowed to manipulate
 * the socket's connection should interact with the socket exclusively via this interface.
 * Created by gabriel on 9/15/17.
 */

interface WebSocketEventPublisher {
    fun setListener(listener: WebSocketEventListener)

    /**
     * Clear the current listener reference if it is the same as the provided reference.
     * @param listener the reference to compare with. If it is the same as the one currently held
     * it is released.
     */
    fun clearListener(listener: WebSocketEventListener)

}
