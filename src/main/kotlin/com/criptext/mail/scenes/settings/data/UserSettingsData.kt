package com.criptext.mail.scenes.settings.data

import com.criptext.mail.scenes.settings.devices.DeviceItem
import org.json.JSONObject

data class UserSettingsData(val devices: List<DeviceItem>, val recoveryEmail: String,
                            val recoveryEmailConfirmationState: Boolean){
    companion object {
        fun fromJSON(metadataString: String): UserSettingsData {
            val metadataJson = JSONObject(metadataString)
            val devicesData = metadataJson.getJSONArray("devices")
            val devices = (0..(devicesData.length()-1))
                    .map {
                        val json = devicesData.getJSONObject(it)
                        DeviceItem(json.getInt("deviceId"),
                                json.getInt("deviceType"), json.getString("deviceFriendlyName"),
                                json.getString("deviceName"), false)
                    }
            val recoveryEmail = metadataJson.getJSONObject("recoveryEmail")
            return UserSettingsData(devices.filter { it.isCurrent } + devices.filter { !it.isCurrent }, recoveryEmail.getString("address"),
                    recoveryEmail.getInt("status") == 1)
        }
    }
}