package com.criptext.mail.websocket

import com.criptext.mail.api.models.*
import com.criptext.mail.db.models.Email
import com.criptext.mail.scenes.signin.data.LinkStatusData
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
    fun onNewEvent(recipientId: String, domain: String)

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
     * Invoked when the recovery email has changed. Subscribers should try to
     * add the update to the list of notifications in the UI.
     * @param newEmail: New recovery email.
     */
    fun onRecoveryEmailChanged(newEmail: String)

    /**
     * Invoked when the recovery email has been confirmed. Subscribers should try to
     * add the update to the list of notifications in the UI.
     */
    fun onRecoveryEmailConfirmed()

    /**
     * Invoked when an untrusted device is requesting a link device process. Subscribers should try to
     * add the update to the list of notifications in the UI.
     */
    fun onDeviceLinkAuthRequest(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo)

    /**
     * Invoked when the link request has been accepted by a trusted device. Subscribers should try to
     * add the update to the list of notifications in the UI.
     */
    fun onDeviceLinkAuthAccept(linkStatusData: LinkStatusData)

    /**
     * Invoked when the link request has been denied by a trusted device. Subscribers should try to
     * add the update to the list of notifications in the UI.
     */
    fun onDeviceLinkAuthDeny()

    /**
     * Invoked when the old device has uploaded its encrypted data to the data transfer server. Subscribers should try to
     * add the update to the list of notifications in the UI.
     */
    fun onDeviceDataUploaded(key: String, dataAddress: String, authorizerId: Int)

    /**
     * Invoked when the new device trying to sync/link has uploaded its keybundle. Subscribers should try to
     * add the update to the list of notifications in the UI.
     */
    fun onKeyBundleUploaded(deviceId: Int)

    /**
     * Invoked when there is a manual mailbox sync request.
     */
    fun onSyncBeginRequest(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo)

    /**
     * Invoked when the sync request has been accepted by a trusted device. Subscribers should try to
     * add the update to the list of notifications in the UI.
     */
    fun onSyncRequestAccept(syncStatusData: SyncStatusData)

    /**
     * Invoked when the sync request has been denied by a trusted device. Subscribers should try to
     * add the update to the list of notifications in the UI.
     */
    fun onSyncRequestDeny()

    /**
     * Invoked when the enterprise account has been suspended. Subscribers should try to
     * add the update to the list of notifications in the UI.
     */
    fun onAccountSuspended(accountEmail: String)

    /**
     * Invoked when the enterprise account has been unsuspended. Subscribers should try to
     * add the update to the list of notifications in the UI.
     */
    fun onAccountUnsuspended(accountEmail: String)

    /**
     * Called when something went wrong processing the event. Subscribers may want to display an
     * error message.
     * @param uiMessage: Object with localized error message
     */
    fun onError(uiMessage: UIMessage)
}
