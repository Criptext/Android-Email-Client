package com.criptext.mail.scenes.settings.devices

import com.criptext.mail.utils.DeviceUtils
import org.json.JSONArray

data class DeviceItem(val id: Int, val deviceType: Int, val friendlyName: String, val name: String, val isCurrent: Boolean) {

    constructor() : this(0, DeviceUtils.getDeviceType().ordinal,
            DeviceUtils.getDeviceFriendlyName(), DeviceUtils.getDeviceName(), true)


    companion object {
        fun fromJSON(metadataString: String): List<DeviceItem> {
            val devicesData = JSONArray(metadataString)
            val devices = (0..(devicesData.length()-1))
                    .map {
                        val json = devicesData.getJSONObject(it)
                        DeviceItem(json.getInt("deviceId"),
                                json.getInt("deviceType"), json.getString("deviceFriendlyName"),
                                json.getString("deviceName"), false)
                    }
            return (devices.filter { it.isCurrent } + devices.filter { !it.isCurrent })
        }
    }
}