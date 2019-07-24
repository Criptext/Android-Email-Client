package com.criptext.mail.scenes.settings.data

import com.criptext.mail.scenes.settings.devices.data.DeviceItem
import com.criptext.mail.utils.DateAndTimeUtils
import org.json.JSONObject

data class UserSettingsData(val devices: List<DeviceItem>, val recoveryEmail: String,
                            val recoveryEmailConfirmationState: Boolean, val hasTwoFA: Boolean,
                            val hasReadReceipts: Boolean, val replyTo: String?){
    companion object {
        fun fromJSON(metadataString: String): UserSettingsData {
            val metadataJson = JSONObject(metadataString)
            val devicesData = metadataJson.getJSONArray("devices")
            val devices = DeviceItem.fromJSON(devicesData.toString())
            val general = metadataJson.getJSONObject("general")
            val recoveryEmail = general.getString("recoveryEmail")
            val replyToEmail = if(general.isNull("replyTo")) null else general.getString("replyTo")
            val recoveryEmailConfirmationState = general.getInt("recoveryEmailConfirmed") == 1
            val twoFactorAuth = general.getInt("twoFactorAuth") == 1
            val trackEmailRead = general.getInt("trackEmailRead") == 1
            return UserSettingsData(devices, recoveryEmail,
                    recoveryEmailConfirmationState, twoFactorAuth, trackEmailRead, replyToEmail)
        }
    }
}