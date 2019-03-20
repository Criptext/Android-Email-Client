package com.criptext.mail.scenes.settings.devices.data

sealed class DevicesRequest{
    data class RemoveDevice(val deviceId: Int, val position: Int, val password: String): DevicesRequest()
}