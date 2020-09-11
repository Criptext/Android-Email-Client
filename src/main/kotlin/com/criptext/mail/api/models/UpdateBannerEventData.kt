package com.criptext.mail.api.models

import org.json.JSONObject

data class UpdateBannerEventData(val messageCode: Int, val version: String,  val operator: Int){
    companion object {
        fun fromJSON(jsonString: String): UpdateBannerEventData {
            val json = JSONObject(jsonString)
            return UpdateBannerEventData(
                    messageCode = json.getInt("code"),
                    version = json.getString("version"),
                    operator = json.getString("operator").toInt()
            )
        }
    }
}