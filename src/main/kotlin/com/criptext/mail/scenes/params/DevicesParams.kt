package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.settings.devices.DevicesActivity
import com.criptext.mail.scenes.settings.devices.data.DeviceItem

class DevicesParams(val devices: ArrayList<DeviceItem>): SceneParams(){
    override val activityClass = DevicesActivity::class.java
}