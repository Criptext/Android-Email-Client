package com.criptext.mail.utils.remotechange.data

/**
 * Created by gabriel on 5/1/18.
 */
sealed class RemoteChangeRequest {
    class DeviceRemoved: RemoteChangeRequest()
    data class ConfirmPassword(val password: String): RemoteChangeRequest()
}