package com.criptext.mail.push.data

import com.criptext.mail.utils.DeviceUtils

sealed class IntentExtrasData(open val action: String) {

    data class IntentExtrasDataMail(override val action: String, val threadId: String) : IntentExtrasData(action)
    data class IntentExtrasDataDevice(override val action: String, val deviceId: String,
                                      val deviceType: DeviceUtils.DeviceType) : IntentExtrasData(action)

}