package com.criptext.mail.scenes.mailbox

import com.criptext.mail.BuildConfig
import com.criptext.mail.db.models.Contact
import com.criptext.mail.utils.DeviceUtils

class SupportMailTemplate {
    private val doNotWrite = "Do not write below this line."
    private val deviceName: String = DeviceUtils.getDeviceName()
    private val deviceOS: String = DeviceUtils.getDeviceOS()
    private val version: String = BuildConfig.VERSION_NAME

    val subject = "Criptext Android Support"
    val contact = "support@${Contact.mainDomain}"
    val body: String
        get() = "<br/><br/><br/><br/><br/><br/><br/>$doNotWrite<br/>****************************<br/>Version: $version<br/>" +
                "Device: $deviceName<br/>OS: $deviceOS<br/>"

}