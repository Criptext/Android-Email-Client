package com.criptext.mail.push.data

sealed class IntentExtrasData(open val action: String) {

    data class IntentExtrasDataMail(override val action: String, val threadId: String) : IntentExtrasData(action)
    data class IntentExtrasDataDevice(override val action: String, val deviceId: String) : IntentExtrasData(action)

}