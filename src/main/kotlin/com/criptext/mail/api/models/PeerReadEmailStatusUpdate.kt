package com.criptext.mail.api.models


import org.json.JSONArray
import org.json.JSONObject

/**
 * * data class for per email status updates. This is received as params of a "peer email read status update" event (301).
 */

data class PeerReadEmailStatusUpdate (val metadataKeys: List<Long>, val unread: Boolean) {
    companion object {

        fun fromJSON(jsonString: String): PeerReadEmailStatusUpdate {
            val json = JSONObject(jsonString)

            return PeerReadEmailStatusUpdate(
                    metadataKeys =  fromJSONArray(json.getJSONArray("metadataKeys")).map { it.toLong() },
                    unread = json.getInt("unread") == 1
            )
        }

        private fun fromJSONArray(jsonArray: JSONArray): List<String> {
            val length = jsonArray.length()
            return (0..(length-1))
                    .map {
                        val json = jsonArray.get(it)
                        json.toString()
                    }
        }
    }
}