package com.criptext.mail.scenes.signin.data

import com.criptext.mail.scenes.settings.devices.data.DeviceItem
import com.criptext.mail.scenes.signin.SignInSceneModel
import com.criptext.mail.utils.virtuallist.VirtualList

class VirtualDeviceList(val model: SignInSceneModel): VirtualList<DeviceItem>{

    override fun get(i: Int): DeviceItem {
        return model.devices[i]
    }

    override val size: Int
        get() = model.devices.size

    override val hasReachedEnd = true

}