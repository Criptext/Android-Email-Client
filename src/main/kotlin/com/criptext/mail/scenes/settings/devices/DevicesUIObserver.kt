package com.criptext.mail.scenes.settings.devices

import com.criptext.mail.utils.uiobserver.UIObserver

interface DevicesUIObserver: UIObserver {
    fun onBackButtonPressed()
    fun onRemoveDeviceConfirmed(deviceId: Int, position: Int, password: String)
    fun onRemoveDeviceCancel()
    fun onRemoveDevice(deviceId: Int, position: Int)
}