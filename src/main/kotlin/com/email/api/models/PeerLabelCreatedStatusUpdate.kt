package com.email.api.models


import org.json.JSONObject

/**
 * * data class for per email status updates. This is received as params of a "peer label created status update" event (308).
 */

data class PeerLabelCreatedStatusUpdate (val text: String, val color: String) {
    companion object {

        fun fromJSON(jsonString: String): PeerLabelCreatedStatusUpdate {
            val json = JSONObject(jsonString)
            return PeerLabelCreatedStatusUpdate(
                    text = json.getString("text"),
                    color = json.getString("color")
            )
        }
    }
}