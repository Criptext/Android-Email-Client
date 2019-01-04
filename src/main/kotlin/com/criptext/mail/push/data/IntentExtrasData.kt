package com.criptext.mail.push.data

import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.UIMessage

sealed class IntentExtrasData(open val action: String) {

    data class IntentExtrasDataMail(override val action: String, val threadId: String) : IntentExtrasData(action)
    data class IntentExtrasDataDevice(override val action: String, val deviceId: String,
                                      val deviceType: DeviceUtils.DeviceType, val syncFileVersion: Int) : IntentExtrasData(action)
    data class IntentExtrasSyncDevice(override val action: String, val randomId: String, val deviceId: Int, val deviceName: String,
                                      val deviceType: DeviceUtils.DeviceType, val syncFileVersion: Int) : IntentExtrasData(action)
    data class IntentExtrasReply(override val action: String, val threadId: String, val metadataKey: Long) : IntentExtrasData(action)
    data class IntentExtrasMailTo(override val action: String, val mailTo: String) : IntentExtrasData(action)
    data class IntentExtrasSend(override val action: String, val files: List<Pair<String, Long>>) : IntentExtrasData(action)
    data class IntentErrorMessage(override val action: String, val uiMessage: UIMessage) : IntentExtrasData(action)

}