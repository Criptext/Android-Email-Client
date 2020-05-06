package com.criptext.mail.utils.generaldatasource.data

import org.json.JSONObject

data class BackupFileMetadata(val fileVersion: Int, val recipientId: String, val domain: String,
                              val language: String, val darkTheme: Boolean, val signature: String,
                              val showPreview: Boolean, val hasCriptextFooter: Boolean){
    companion object {
        fun fromJSON(jsonString: String): BackupFileMetadata{
            val json = JSONObject(jsonString)
            return BackupFileMetadata(
                    fileVersion = json.getInt("fileVersion"),
                    recipientId = json.getString("recipientId"),
                    domain = json.getString("domain"),
                    signature = json.getString("signature"),
                    darkTheme = json.getBoolean("darkTheme"),
                    hasCriptextFooter = json.getBoolean("hasCriptextFooter"),
                    language = json.getString("language"),
                    showPreview = json.getBoolean("showPreview")
            )
        }

        fun toJSON(data: BackupFileMetadata): JSONObject {
            val json = JSONObject()
            json.put("fileVersion", data.fileVersion)
            json.put("recipientId", data.recipientId)
            json.put("domain", data.domain)
            json.put("signature", data.signature)
            json.put("darkTheme", data.darkTheme)
            json.put("hasCriptextFooter", data.hasCriptextFooter)
            json.put("language", data.language)
            json.put("showPreview", data.showPreview)
            return json
        }
    }
}