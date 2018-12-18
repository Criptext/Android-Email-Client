package com.criptext.mail.api.models


import com.criptext.mail.utils.DateAndTimeUtils
import org.json.JSONObject
import java.util.*

/**
 * * data class for per email status updates. This is received as params of a "peer email status update" event (307).
 */

data class PeerUnsendEmailStatusUpdate (val metadataKey: Long, val unsendDate: Date) {
    companion object {

        fun fromJSON(jsonString: String): PeerUnsendEmailStatusUpdate {
            val json = JSONObject(jsonString)
            return PeerUnsendEmailStatusUpdate(
                    metadataKey = json.getLong("metadataKey"),
                    unsendDate = DateAndTimeUtils.getDateFromString(json.getString("date"), null)
            )
        }
    }
}