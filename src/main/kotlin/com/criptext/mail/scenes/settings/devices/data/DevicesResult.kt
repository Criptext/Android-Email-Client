package com.criptext.mail.scenes.settings.devices.data

import com.criptext.mail.utils.UIMessage

sealed class DevicesResult{

    sealed class RemoveDevice : DevicesResult() {
        class Success(val deviceId: Int, val position: Int): RemoveDevice()
        data class Failure(val message: UIMessage): RemoveDevice()
        class EnterpriseSuspend: RemoveDevice()
    }
}