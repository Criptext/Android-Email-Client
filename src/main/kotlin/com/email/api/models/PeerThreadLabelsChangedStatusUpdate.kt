package com.email.api.models


import org.json.JSONArray
import org.json.JSONObject

/**
 * * data class for per email status updates. This is received as params of a "peer thread read status update" event (303).
 */

data class PeerThreadLabelsChangedStatusUpdate (val threadIds: List<String>, val labelsRemoved: List<String>, val labelsAdded: List<String>) {
    companion object {

        fun fromJSON(jsonString: String): PeerThreadLabelsChangedStatusUpdate {
            val json = JSONObject(jsonString)
            return PeerThreadLabelsChangedStatusUpdate(
                    threadIds =  fromJSONArray(json.getJSONArray("threadIds")),
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