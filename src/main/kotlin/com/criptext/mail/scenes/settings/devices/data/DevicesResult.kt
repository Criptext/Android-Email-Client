package com.criptext.mail.scenes.settings.devices.data

sealed class DevicesResult{

    sealed class RemoveDevice : DevicesResult() {
        class Success(val deviceId: Int, val position: Int): RemoveDevice()
        class Failure: RemoveDevice()
        class EnterpriseSuspend: RemoveDevice()
    }
}