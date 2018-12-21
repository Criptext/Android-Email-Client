package com.criptext.mail.scenes.settings

import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.settings.devices.DeviceItem
import com.criptext.mail.validation.FormInputState

class SettingsModel{
    var fullName: String = ""
    var signature: String = ""
    val labels : ArrayList<LabelWrapper> = ArrayList()
    val devices: ArrayList<DeviceItem> = ArrayList()

    var oldPasswordText: String = ""
    var passwordText: String = ""
    var confirmPasswordText: String = ""
    var passwordState: FormInputState = FormInputState.Unknown()

    var isEmailConfirmed: Boolean = false
    var hasTwoFA: Boolean = false
    var hasReadReceipts: Boolean = false
    var recoveryEmail: String = ""
    var hasChangedTheme: Boolean = false
}