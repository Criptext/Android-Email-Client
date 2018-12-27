package com.criptext.mail.utils.mailtemplates

import android.content.Context
import com.criptext.mail.BuildConfig
import com.criptext.mail.R
import com.criptext.mail.db.models.Contact
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage

class FWMailTemplate(ctx: Context) : CriptextMailTemplate(ctx) {
    val from = ctx.getLocalizedUIMessage(UIMessage(R.string.from_popup))
    val date = ctx.getLocalizedUIMessage(UIMessage(R.string.date_popup))
    val subject = ctx.getLocalizedUIMessage(UIMessage(R.string.subject_popup))
    val to = ctx.getLocalizedUIMessage(UIMessage(R.string.to_popup))
    val at = ctx.getLocalizedUIMessage(UIMessage(R.string.mail_template_at))
    val message = ctx.getLocalizedUIMessage(UIMessage(R.string.forwarded_message))

}