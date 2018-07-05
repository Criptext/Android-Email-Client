package com.email.scenes.settings

import com.email.scenes.label_chooser.data.LabelWrapper
import com.email.scenes.settings.devices.DeviceItem

class SettingsModel{
    var fullName: String = ""
    var signature: String = ""
    val labels : ArrayList<LabelWrapper> = ArrayList()
    val devices: ArrayList<DeviceItem> = ArrayList()
}