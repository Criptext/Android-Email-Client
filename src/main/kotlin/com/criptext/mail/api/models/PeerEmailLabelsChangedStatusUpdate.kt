package com.criptext.mail.api.models


import org.json.JSONArray
import org.json.JSONObject

/**
 * * data class for per email status updates. This is received as params of a "peer thread read status update" event (303).
 */

data class PeerEmailLabelsChangedStatusUpdate (val metadataKeys: List<Long>, val labelsRemoved: List<String>, val labelsAdded: List<String>) {
    companion object {

        fun fromJSON(jsonString: String): PeerEmailLabelsChangedStatusUpdate {
            val json = JSONObject(jsonString)
            return PeerEmailLabelsChangedStatusUpdate(
                    metadataKeys =  fromJSONArray(json.getJSONArray("metadataKeys")).map { it.toLong() },
                    labelsAdded = fromJSONArray(json.getJSONArray("labelsAdded")),
                    labelsRemoved = fromJSONArray(json.getJSONArray("labelsRemoved"))
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