package com.criptext.mail.utils.mailtemplates

import android.content.Context

abstract class CriptextMailTemplate(protected val ctx: Context){
    enum class TemplateType{SUPPORT, FW, RE, ABUSE}
}