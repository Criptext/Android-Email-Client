package com.criptext.mail.utils.mailtemplates

import android.content.Context
import com.criptext.mail.R
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage

class REMailTemplate(ctx: Context) : CriptextMailTemplate(ctx) {
    val on = ctx.getLocalizedUIMessage(UIMessage(R.string.reply_on))
    val wrote = ctx.getLocalizedUIMessage(UIMessage(R.string.reply_wrote))
    val at = ctx.getLocalizedUIMessage(UIMessage(R.string.mail_template_at))
}