package com.criptext.mail.scenes.signin.data

import com.criptext.mail.utils.DeviceUtils
import org.json.JSONObject

data class LinkStatusData(val deviceId: Int, val name: String, val authorizerId: Int,
                          val authorizerName: String, val authorizerType: DeviceUtils.DeviceType){

    companion object {
        fun fromJSON(string: String): LinkStatusData {
            val json = JSONObject(string)
            return LinkStatusData(json.getInt("deviceId"), json.getString("name"),
                    json.getInt("authorizerId"), json.getString("authorizerName"),
                    DeviceUtils.getDeviceType(json.getInt("authorizerType")))
        }
    }
}