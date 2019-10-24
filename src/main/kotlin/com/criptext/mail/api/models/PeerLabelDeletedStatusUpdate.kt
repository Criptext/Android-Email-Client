package com.criptext.mail.api.models


import org.json.JSONObject

/**
 * * data class for per label status updates. This is received as params of a "peer label deleted status update" event (320).
 */

data class PeerLabelDeletedStatusUpdate (val uuid: String) {
    companion object {

        fun fromJSON(jsonString: String): PeerLabelDeletedStatusUpdate {
            val json = JSONObject(jsonString)
            return PeerLabelDeletedStatusUpdate(
                    uuid = json.getString("uuid")
            )
        }
    }
}