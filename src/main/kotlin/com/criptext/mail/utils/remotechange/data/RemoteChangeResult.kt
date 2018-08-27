package com.criptext.mail.utils.remotechange.data

/**
 * Created by gabriel on 5/1/18.
 */
sealed class RemoteChangeResult {
    sealed class DeviceRemoved: RemoteChangeResult()  {
        class Success: DeviceRemoved()
        class Failure: DeviceRemoved()
    }

    sealed class ConfirmPassword: RemoteChangeResult()  {
        class Success: ConfirmPassword()
        class Failure: ConfirmPassword()
    }
}