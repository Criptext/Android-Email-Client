package com.criptext.mail.api.models


import com.criptext.mail.utils.DeviceUtils
import org.json.JSONObject


data class UntrustedDeviceInfo (val deviceId: String, val recipientId: String, val deviceName: String, val deviceFriendlyName: String,
                                val deviceType: DeviceUtils.DeviceType) {
    companion object {

        fun fromJSON(jsonString: String): UntrustedDeviceInfo {
            val json = JSONObject(jsonString)
                    .getJSONObject("newDeviceInfo")
            return UntrustedDeviceInfo(
                    deviceId = json.getJSONObject("session").getString("randomId"),
                    recipientId = json.getString("recipientId"),
                    deviceName = json.getString("deviceName"),
                    deviceFriendlyName = json.getString("deviceFriendlyName"),
                    deviceType = DeviceUtils.getDeviceType(json.getInt("deviceType"))
            )
        }
    }
}