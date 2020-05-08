package com.criptext.mail.scenes.settings

import com.criptext.mail.scenes.settings.devices.data.DeviceItem

interface DevicesListItemListener {
    fun onDeviceTrashClicked(device: DeviceItem, position: Int) : Boolean
    fun onDeviceCheckChanged() : Boolean
}