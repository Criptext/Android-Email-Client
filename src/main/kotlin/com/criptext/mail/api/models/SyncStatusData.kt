package com.criptext.mail.api.models

import com.criptext.mail.utils.DeviceUtils
import org.json.JSONObject

data class SyncStatusData(val randomId: String, val authorizerId: Int,
                          val authorizerName: String, val authorizerType: DeviceUtils.DeviceType){

    companion object {
        fun fromJSON(string: String): SyncStatusData {
            val json = JSONObject(string)
            return SyncStatusData(
                    randomId = json.getString("randomId"),
                    authorizerId = json.getInt("authorizerId"),
                    authorizerName = json.getString("authorizerName"),
                    authorizerType = DeviceUtils.getDeviceType(json.getInt("authorizerType"))
            )
        }
    }
}