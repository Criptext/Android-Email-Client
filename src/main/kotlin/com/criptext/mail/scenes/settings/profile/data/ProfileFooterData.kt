package com.criptext.mail.scenes.settings.profile.data

import org.json.JSONArray
import org.json.JSONObject

data class ProfileFooterData(val accountId: Long, val hasFooterEnabled: Boolean) {

    companion object {
        fun toJSON(list: List<ProfileFooterData>): JSONObject{
            val returnJson = JSONObject()
            val jsonArray = JSONArray()
            list.forEach {
                val json = JSONObject()
                json.put("accountId", it.accountId)
                json.put("hasFooterEnabled", it.hasFooterEnabled)
                jsonArray.put(json)
            }
            returnJson.put("profileFooterData", jsonArray)
            return returnJson
        }

        fun fromJson(jsonString: String): MutableList<ProfileFooterData>{
            val json = JSONObject(jsonString)
            val jsonArray = if(json.has("profileFooterData")) json.getJSONArray("profileFooterData")
                            else return mutableListOf()
            val length = jsonArray.length()
            val savedDataList = mutableListOf<ProfileFooterData>()
            (0 until length)
                    .map {
                        val jsonItem = jsonArray.getJSONObject(it)
                        savedDataList.add(ProfileFooterData(
                                jsonItem.getLong("accountId"),
                                jsonItem.getBoolean("hasFooterEnabled")
                        ))
                    }
            return savedDataList
        }
    }
}