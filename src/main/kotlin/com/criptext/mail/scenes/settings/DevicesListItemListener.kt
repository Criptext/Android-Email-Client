package com.criptext.mail.scenes.settings

import com.criptext.mail.scenes.settings.devices.DeviceItem

interface DevicesListItemListener {
    fun onDeviceTrashClicked(device: DeviceItem, position: Int) : Boolean
}