package com.criptext.mail.scenes.settings.devices.data

import com.criptext.mail.scenes.settings.devices.DevicesModel
import com.criptext.mail.utils.virtuallist.VirtualList

class VirtualDeviceList(val model: DevicesModel): VirtualList<DeviceItem>{

    override fun get(i: Int): DeviceItem {
        return model.devices[i]
    }

    override val size: Int
        get() = model.devices.size

    override val hasReachedEnd = true

}