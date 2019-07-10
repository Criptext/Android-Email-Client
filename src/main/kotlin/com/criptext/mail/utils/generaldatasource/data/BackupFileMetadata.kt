package com.criptext.mail.utils.generaldatasource.data

import org.json.JSONObject

data class BackupFileMetadata(val fileVersion: Int, val recipientId: String, val domain: String){
    companion object {
        fun fromJSON(jsonString: String): BackupFileMetadata{
            val json = JSONObject(jsonString)
            return BackupFileMetadata(
                    fileVersion = json.getInt("fileVersion"),
                    recipientId = json.getString("recipientId"),
                    domain = json.getString("domain")
            )
        }

        fun toJSON(data: BackupFileMetadata): JSONObject {
            val json = JSONObject()
            json.put("fileVersion", data.fileVersion)
            json.put("recipientId", data.recipientId)
            json.put("domain", data.domain)
            return json
        }
    }
}