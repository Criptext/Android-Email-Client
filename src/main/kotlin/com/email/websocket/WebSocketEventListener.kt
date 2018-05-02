package com.email.websocket

import com.email.db.models.Email
import com.email.utils.UIMessage

/**
 * Objects  that implement this interface can subscribe to the web socket and react to events emitted
 * by the web socket.
 * Created by gabriel on 9/15/17.
 */
interface WebSocketEventListener{
    /**
     * Invoked when an email has been received. Subscribers should show the email
     * in the UI to notify the user about it.
     * @param token the token of the opened email
     * @param message the message that should be shown in the UI.
     */
    fun onNewEmail(email: Email)

    /**
     * Called when something went wrong processing the event. Subscribers may want to display an
     * error message.
     * @param uiMessage: Object with localized error message
     */
    fun onError(uiMessage: UIMessage)
}
