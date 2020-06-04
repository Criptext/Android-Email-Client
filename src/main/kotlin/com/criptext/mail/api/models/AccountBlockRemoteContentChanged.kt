package com.criptext.mail.api.models

import org.json.JSONObject

data class AccountBlockRemoteContentChanged(val recipientId: String, val domain: String, val newBlockRemoteContent: Boolean){
    companion object {
        fun fromJSON(jsonString: String): AccountBlockRemoteContentChanged {
            val json = JSONObject(jsonString)
            return AccountBlockRemoteContentChanged(
                    recipientId = json.getString("recipientId"),
                    domain = json.getString("domain"),
                    newBlockRemoteContent = json.getBoolean("block")
            )
        }
    }
}