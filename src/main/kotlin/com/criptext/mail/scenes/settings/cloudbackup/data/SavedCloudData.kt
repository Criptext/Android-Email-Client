package com.criptext.mail.scenes.settings.cloudbackup.data

import org.json.JSONArray
import org.json.JSONObject

data class SavedCloudData(val accountId: Long, val backupSize: Long, val lastModified: Long) {

    companion object {
        fun toJSON(list: List<SavedCloudData>): JSONObject{
            val returnJson = JSONObject()
            val jsonArray = JSONArray()
            list.forEach {
                val json = JSONObject()
                json.put("accountId", it.accountId)
                json.put("backupSize", it.backupSize)
                json.put("lastModified", it.lastModified)
                jsonArray.put(json)
            }
            returnJson.put("savedCloudData", jsonArray)
            return returnJson
        }

        fun fromJson(jsonString: String): List<SavedCloudData>{
            val json = JSONObject(jsonString)
            val jsonArray = json.getJSONArray("savedCloudData")
            val length = jsonArray.length()
            val savedDataList = mutableListOf<SavedCloudData>()
            (0..(length-1))
                    .map {
                        val jsonItem = jsonArray.getJSONObject(it)
                        savedDataList.add(SavedCloudData(
                                jsonItem.getLong("accountId"),
                                jsonItem.getLong("backupSize"),
                                jsonItem.getLong("lastModified")
                        ))
                    }
            return savedDataList
        }
    }
}