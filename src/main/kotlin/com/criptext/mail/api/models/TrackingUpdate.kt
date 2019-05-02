package com.criptext.mail.api.models

import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.utils.DateAndTimeUtils
import org.json.JSONObject
import java.util.*

/**
 * * data class for tracking updates. This is received as params of a "tracking update" event (2).
 * Created by gabriel on 6/28/18.
 */

data class TrackingUpdate (val metadataKey: Long, val type: DeliveryTypes, val date: Date, val from: String) {
    companion object {

        fun fromJSON(jsonString: String): TrackingUpdate {
            val json = JSONObject(jsonString)
            val jsonFrom = json.getJSONObject("fromDomain")
            return TrackingUpdate(
                    metadataKey = json.getLong("metadataKey"),
                    from = jsonFrom.getString("recipientId").plus("@${jsonFrom.getString("domain")}"),
                    type = DeliveryTypes.fromInt(json.getInt("type")),
                    date = Date()
            )
        }
    }
}