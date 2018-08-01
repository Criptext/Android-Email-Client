package com.criptext.mail.scenes.settings

import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.settings.devices.DeviceItem

class SettingsModel{
    var fullName: String = ""
    var signature: String = ""
    val labels : ArrayList<LabelWrapper> = ArrayList()
    val devices: ArrayList<DeviceItem> = ArrayList()
}