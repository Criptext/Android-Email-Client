package com.email.websocket

import com.email.db.models.FullEmail
import com.email.scenes.mailbox.data.EmailThread

/**
 * Objects  that implement this interface can subscribe to the web socket and react to events emitted
 * by the web socket.
 * Created by gabriel on 9/15/17.
 */
interface WebSocketEventListener{
    /**
     * Invoked when an email sent by the user has been opened. Subscribers should show the message
     * in the UI to notify the user about it.
     * @param token the token of the opened email
     * @param message the message that should be shown in the UI.
     */
    fun onMailOpened(token : String, message: String)
    /**
     * Invoked when an email attachment sent by the user has been opened or downloaded.
     * Subscribers should show the message in the UI to notify the user about it.
     * @param token the token of the opened email
     * @param message the message that should be shown in the UI. It says "opened" or "downloaded"
     * depending on the event.
     */
    fun onFileOpenedOrDownloaded(mailToken: String, message: String)
    /**
     * Invoked when the user sends a new email. Could have happened in a different device.
     * @param activity the activity object of the sent email
     */
    fun onNewMessage(emailThread: EmailThread)
    //TODO(fun onNewAttachment(attachments: ArrayList<CriptextAttachment>, token: String))
    /**
     * Invoked when the user unsends an email. Could have happened in a different device.
     * @param token the email token of the unsent email
     */
    fun onUnsent(token : String)
    /**
     * Invoked when the user mutes or unmutes an activity notification. Could have happened in a different device.
     */
    fun onMuteMessage()
    /**
     * Invoked when the user changes his/her plan.
     * @param status The new status of the user's plan -1, 0 or 1
     */
    fun onUserStatusChange(status : Int)
}
