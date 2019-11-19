package com.criptext.mail.utils.mailtemplates

import android.content.Context
import com.criptext.mail.BuildConfig
import com.criptext.mail.R
import com.criptext.mail.db.models.Contact
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage

class ReportAbuseMailTemplate(ctx: Context) : CriptextMailTemplate(ctx) {
    private val doNotWrite = ctx.getLocalizedUIMessage(UIMessage(R.string.support_mail_line))
    private val deviceName: String = DeviceUtils.getDeviceName()
    private val deviceOS: String = DeviceUtils.getDeviceOS()
    private val version: String = BuildConfig.VERSION_NAME

    val subject = "Criptext Report Abuse"
    val contact = "abuse@${Contact.mainDomain}"
    val body: String
        get() = "<br/><br/><br/><br/><br/><br/><br/>$doNotWrite<br/>****************************<br/>Version: $version<br/>" +
                "Device: $deviceName<br/>OS: $deviceOS<br/>"

}