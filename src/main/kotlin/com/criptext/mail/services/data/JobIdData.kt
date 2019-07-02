package com.criptext.mail.services.data

import org.json.JSONArray
import org.json.JSONObject

data class JobIdData(val accountId: Long, val jobId: Int, var useWifiOnly: Boolean = true) {

    companion object {
        fun toJSON(list: List<JobIdData>): JSONObject{
            val returnJson = JSONObject()
            val jsonArray = JSONArray()
            list.forEach {
                val json = JSONObject()
                json.put("accountId", it.accountId)
                json.put("jobId", it.jobId)
                json.put("useWifiOnly", it.useWifiOnly)
                jsonArray.put(json)
            }
            returnJson.put("jobIdData", jsonArray)
            return returnJson
        }

        fun fromJson(jsonString: String): MutableList<JobIdData>{
            val json = JSONObject(jsonString)
            val jsonArray = json.getJSONArray("jobIdData")
            val length = jsonArray.length()
            val savedDataList = mutableListOf<JobIdData>()
            (0 until length)
                    .map {
                        val jsonItem = jsonArray.getJSONObject(it)
                        savedDataList.add(JobIdData(
                                jsonItem.getLong("accountId"),
                                jsonItem.getInt("jobId"),
                                if(jsonItem.has("useWifiOnly"))
                                    jsonItem.getBoolean("useWifiOnly")
                                else
                                    true
                        ))
                    }
            return savedDataList
        }
    }
}