package com.criptext.mail.scenes.settings.devices

import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.DeviceUtils
import org.json.JSONArray
import java.util.*

data class DeviceItem(val id: Int, val deviceType: Int, val friendlyName: String, val name: String,
                      val isCurrent: Boolean, val lastActivity: Date?) {

    constructor() : this(0, DeviceUtils.getDeviceType().ordinal,
            DeviceUtils.getDeviceFriendlyName(), DeviceUtils.getDeviceName(), true, null)


    companion object {
        fun fromJSON(metadataString: String): List<DeviceItem> {
            val devicesData = JSONArray(metadataString)
            val devices = (0..(devicesData.length()-1))
                    .map {
                        val json = devicesData.getJSONObject(it)
                        val jsonDate = json.getJSONObject("lastActivity").optString("date")
                        val date = if(jsonDate != null)
                            DateAndTimeUtils.getDateFromString(jsonDate, null)
                        else
                            null
                        DeviceItem(json.getInt("deviceId"),
                                json.getInt("deviceType"), json.getString("deviceFriendlyName"),
                                json.getString("deviceName"), false, date)
                    }
            return (devices.filter { it.isCurrent } + devices.filter { !it.isCurrent })
        }
    }
}