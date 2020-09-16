package com.criptext.mail.api.models

import org.json.JSONObject

data class ActionRequiredEventData(val messageCode: Int){
    companion object {
        fun fromJSON(jsonString: String): ActionRequiredEventData {
            val json = JSONObject(jsonString)
            return ActionRequiredEventData(
                    messageCode = json.getInt("code")
            )
        }
    }
}