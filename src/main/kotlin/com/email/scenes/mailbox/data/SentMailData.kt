package com.email.scenes.mailbox.data

import org.json.JSONObject

/**
 * Created by gabriel on 5/22/18.
 */
data class SentMailData(val date: String, val metadataKey: Long, val messageId: String,
                        val threadId: String) {

    fun toJSON(): JSONObject {
        val json = JSONObject()
        json.put("date", date)
        json.put("metadataKey", metadataKey)
        json.put("messageId", messageId)
        json.put("threadId", threadId)

        return json
    }

    companion object {
        fun fromJSON(json: JSONObject): SentMailData {
            val date = json.getString("date")
            val metadataKey = json.getLong("metadataKey")
            val messageId = json.getString("messageId")
            val threadId = json.getString("threadId")

            return SentMailData(date, metadataKey, messageId, threadId)
        }
    }
}