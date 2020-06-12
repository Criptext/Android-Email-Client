package com.criptext.mail.scenes.mailbox.data

import org.json.JSONArray
import org.json.JSONObject

data class ProfileAskedForBackupData(val accountId: Long, val hasAskedForBackup: Boolean) {

    companion object {
        fun toJSON(list: List<ProfileAskedForBackupData>): JSONObject{
            val returnJson = JSONObject()
            val jsonArray = JSONArray()
            list.forEach {
                val json = JSONObject()
                json.put("accountId", it.accountId)
                json.put("hasAskedForBackup", it.hasAskedForBackup)
                jsonArray.put(json)
            }
            returnJson.put("profileAskedForBackupData", jsonArray)
            return returnJson
        }

        fun fromJson(jsonString: String): MutableList<ProfileAskedForBackupData>{
            val json = JSONObject(jsonString)
            val jsonArray = json.getJSONArray("profileAskedForBackupData")
            val length = jsonArray.length()
            val savedDataList = mutableListOf<ProfileAskedForBackupData>()
            (0 until length)
                    .map {
                        val jsonItem = jsonArray.getJSONObject(it)
                        savedDataList.add(ProfileAskedForBackupData(
                                jsonItem.getLong("accountId"),
                                jsonItem.getBoolean("hasAskedForBackup")
                        ))
                    }
            return savedDataList
        }
    }
}