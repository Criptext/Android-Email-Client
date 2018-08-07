package com.criptext.mail.api.models


import org.json.JSONObject

data class FirebaseServiceNotificationData (val metadataKey: Long, val action: String, val body: String,
                                            val title: String, val threadId: String) {
    companion object {

        fun fromJSON(jsonString: String?): FirebaseServiceNotificationData {
            val json = JSONObject(jsonString)
            return FirebaseServiceNotificationData(
                    metadataKey =  json.getLong("metadataKey"),
                    action =  json.getString("action"),
                    body =  json.getString("body"),
                    title =  json.getString("title"),
                    threadId =  json.getString("threadId")
            )
        }
    }
}