package com.criptext.mail.api.models


import org.json.JSONObject

/**
 * * data class for per email status updates. This is received as params of a "peer thread read status update" event (303).
 */

data class PeerUsernameChangedStatusUpdate (val name: String) {
    companion object {

        fun fromJSON(jsonString: String): PeerUsernameChangedStatusUpdate {
            val json = JSONObject(jsonString)
            return PeerUsernameChangedStatusUpdate(
                    name =  json.getString("name")
            )
        }
    }
}