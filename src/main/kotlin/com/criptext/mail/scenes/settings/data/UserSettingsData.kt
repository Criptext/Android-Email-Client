package com.criptext.mail.scenes.settings.data

import com.criptext.mail.db.models.CustomDomain
import com.criptext.mail.scenes.settings.aliases.data.AliasData
import com.criptext.mail.scenes.settings.devices.data.DeviceItem
import com.criptext.mail.utils.DateAndTimeUtils
import org.json.JSONObject

data class UserSettingsData(val devices: List<DeviceItem>, val recoveryEmail: String,
                            val recoveryEmailConfirmationState: Boolean, val hasTwoFA: Boolean,
                            val hasReadReceipts: Boolean, val replyTo: String?, val customDomains: List<CustomDomain>,
                            val aliases: List<AliasData>){
    companion object {
        fun fromJSON(metadataString: String, accountId: Long): UserSettingsData {
            val metadataJson = JSONObject(metadataString)
            val devicesData = metadataJson.getJSONArray("devices")
            val devices = DeviceItem.fromJSON(devicesData.toString())
            val general = metadataJson.getJSONObject("general")
            val recoveryEmail = general.getString("recoveryEmail")
            val replyToEmail = if(general.isNull("replyTo")) null else general.getString("replyTo")
            val recoveryEmailConfirmationState = general.getInt("recoveryEmailConfirmed") == 1
            val twoFactorAuth = general.getInt("twoFactorAuth") == 1
            val trackEmailRead = general.getInt("trackEmailRead") == 1
            val domainsAndAliases = AliasData.fromJSONArray(metadataJson.getJSONArray("addresses"), accountId)
            return UserSettingsData(devices, recoveryEmail,
                    recoveryEmailConfirmationState, twoFactorAuth, trackEmailRead, replyToEmail, domainsAndAliases.first, domainsAndAliases.second)
        }
    }
}