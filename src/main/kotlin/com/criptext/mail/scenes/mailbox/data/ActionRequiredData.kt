package com.criptext.mail.scenes.mailbox.data

import org.json.JSONObject

data class ActionRequiredData(val title: String, val message: String, val image: String){
    companion object {
        fun fromJSON(jsonString: String): ActionRequiredData{
            val json = JSONObject(jsonString)
            return ActionRequiredData(
                    title = json.getString("title"),
                    message = json.getString("body"),
                    image = json.getString("imageUrl")
            )
        }
    }
}