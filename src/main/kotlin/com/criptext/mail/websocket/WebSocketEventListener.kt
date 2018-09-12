package com.criptext.mail.websocket

import com.criptext.mail.api.models.*
import com.criptext.mail.db.models.Email
import com.criptext.mail.utils.UIMessage

/**
 * Objects  that implement this interface can subscribe to the web socket and react to events emitted
 * by the web socket.
 * Created by gabriel on 9/15/17.
 */
interface WebSocketEventListener {

    /**
     * Invoked when an event has been received.
     */
    fun onNewEvent()

    /**
     * Invoked when a new device locked event has been received. Subscribers should try to
     * add the update to the list of notifications in the UI.
     */
    fun onDeviceLocked()

    /**
     * Invoked when a new device removed event has been received. Subscribers should try to
     * add the update to the list of notifications in the UI.
     */
    fun onDeviceRemoved()

    /**
     * Called when something went wrong processing the event. Subscribers may want to display an
     * error message.
     * @param uiMessage: Object with localized error message
     */
    fun onError(uiMessage: UIMessage)
}
