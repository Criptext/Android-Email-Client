package com.criptext.mail.scenes.settings.privacy.data

import org.json.JSONArray
import org.json.JSONObject

data class ExternalEncryptionData(val email: String, val hasEncryption: Boolean){
    companion object {
        fun toJSON(list: List<ExternalEncryptionData>): JSONObject {
            val returnJson = JSONObject()
            val jsonArray = JSONArray()
            list.forEach {
                val json = JSONObject()
                json.put("email", it.email)
                json.put("hasEncryption", it.hasEncryption)
                jsonArray.put(json)
            }
            returnJson.put("externalEncryptionData", jsonArray)
            return returnJson
        }

        fun fromJson(jsonString: String): MutableList<ExternalEncryptionData>{
            val json = JSONObject(jsonString)
            val jsonArray = json.getJSONArray("externalEncryptionData")
            val length = jsonArray.length()
            val savedDataList = mutableListOf<ExternalEncryptionData>()
            (0..(length-1))
                    .map {
                        val jsonItem = jsonArray.getJSONObject(it)
                        savedDataList.add(ExternalEncryptionData(
                                jsonItem.getString("email"),
                                jsonItem.getBoolean("hasEncryption")
                        ))
                    }
            return savedDataList
        }
    }
}