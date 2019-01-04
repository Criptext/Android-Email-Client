package com.criptext.mail.api.models


import com.criptext.mail.utils.DeviceUtils
import org.json.JSONObject


data class TrustedDeviceInfo (val deviceId: Int, val deviceFriendlyName: String,
                              val deviceType: DeviceUtils.DeviceType, val randomId: String, val syncFileVersion: Int) {
    companion object {

        fun fromJSON(jsonString: String): TrustedDeviceInfo {
            val json = JSONObject(jsonString).getJSONObject("requestingDeviceInfo")
            return TrustedDeviceInfo(
                    randomId = JSONObject(jsonString).getString("randomId"),
                    deviceId = json.getInt("deviceId"),
                    deviceFriendlyName = json.getString("deviceFriendlyName"),
                    deviceType = DeviceUtils.getDeviceType(json.getInt("deviceType")),
                    syncFileVersion = JSONObject(jsonString).getInt("version")
            )
        }
    }
}