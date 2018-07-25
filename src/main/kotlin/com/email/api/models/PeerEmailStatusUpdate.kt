package com.email.api.models


import org.json.JSONObject
import java.util.*

/**
 * * data class for per email status updates. This is received as params of a "peer email status update" event (3).
 */

data class PeerEmailStatusUpdate (val metadataKey: Long, val unsendDate: String) {
    companion object {

        fun fromJSON(jsonString: String): PeerEmailStatusUpdate {
            val json = JSONObject(jsonString)
            return PeerEmailStatusUpdate(
                    metadataKey = json.getLong("metadataKey"),
                    unsendDate = json.getString("date")
            )
        }
    }
}