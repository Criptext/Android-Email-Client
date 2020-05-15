package com.criptext.mail.scenes.settings.custom_domain_entry.data

import org.json.JSONObject

data class DomainMXRecordsData(val ttl: String, val host: String, val pointsTo: String, val priority: Int, val type: String){
    companion object {
        fun fromJSON(string: String) : List<DomainMXRecordsData> {
            val jsonObject = JSONObject(string)
            val domainData = jsonObject.getJSONArray("mx")
            val domains = (0 until domainData.length())
                    .map {
                        val json = domainData.getJSONObject(it)
                        DomainMXRecordsData(json.getString("TTL"),
                                json.getString("host"), json.getString("pointsTo"),
                                json.getInt("priority"), json.getString("type"))
                    }
            return domains
        }
    }
}