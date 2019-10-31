package com.criptext.mail.api.models


import org.json.JSONObject

/**
 * * data class for per label status updates. This is received as params of a "peer label deleted status update" event (320).
 */

data class PeerLabelEditedStatusUpdate (val uuid: String, val name: String) {
    companion object {

        fun fromJSON(jsonString: String): PeerLabelEditedStatusUpdate {
            val json = JSONObject(jsonString)
            return PeerLabelEditedStatusUpdate(
                    uuid = json.getString("uuid"),
                    name = json.getString("text")
            )
        }
    }
}