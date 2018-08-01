package com.criptext.mail.scenes.settings.devices

import com.criptext.mail.scenes.settings.SettingsModel
import com.criptext.mail.utils.virtuallist.VirtualList

class VirtualDeviceList(val model: SettingsModel): VirtualList<DeviceItem>{

    override fun get(i: Int): DeviceItem {
        return model.devices[i]
    }

    override val size: Int
        get() = model.devices.size

    override val hasReachedEnd = true

}