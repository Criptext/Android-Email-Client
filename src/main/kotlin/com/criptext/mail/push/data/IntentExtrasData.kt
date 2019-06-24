package com.criptext.mail.push.data

import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.UIMessage

sealed class IntentExtrasData(open val action: String, open val account: String, open val domain: String) {

    data class IntentExtrasDataMail(override val action: String, val threadId: String,
                                    override val account: String, override val domain: String) : IntentExtrasData(action, account, domain)
    data class IntentExtrasDataDevice(override val action: String, val deviceId: String,
                                      val deviceType: DeviceUtils.DeviceType, val syncFileVersion: Int,
                                      override val account: String, override val domain: String) : IntentExtrasData(action, account, domain)
    data class IntentExtrasSyncDevice(override val action: String, val randomId: String, val deviceId: Int, val deviceName: String,
                                      val deviceType: DeviceUtils.DeviceType, val syncFileVersion: Int,
                                      override val account: String, override val domain: String) : IntentExtrasData(action, account, domain)
    data class IntentExtrasReply(override val action: String, val threadId: String, val metadataKey: Long,
                                 override val account: String, override val domain: String) : IntentExtrasData(action, account, domain)
    data class IntentExtrasMailTo(override val action: String, val mailTo: String,
                                  override val account: String, override val domain: String) : IntentExtrasData(action, account, domain)
    data class IntentExtrasSend(override val action: String, val files: List<Pair<String, Long>>, val urls: List<String>,
                                override val account: String, override val domain: String) : IntentExtrasData(action, account, domain)
    data class IntentErrorMessage(override val action: String, val uiMessage: UIMessage,
                                  override val account: String, override val domain: String) : IntentExtrasData(action, account, domain)

}