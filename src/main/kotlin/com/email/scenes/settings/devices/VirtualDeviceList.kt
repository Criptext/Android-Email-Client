package com.email.scenes.settings.devices

import com.email.scenes.settings.SettingsModel
import com.email.utils.virtuallist.VirtualList

class VirtualDeviceList(val model: SettingsModel): VirtualList<DeviceItem>{

    override fun get(i: Int): DeviceItem {
        return model.devices[i]
    }

    override val size: Int
        get() = model.devices.size

    override val hasReachedEnd = true

}