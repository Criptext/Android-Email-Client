package com.criptext.mail.api.models

import com.criptext.mail.db.DeliveryTypes
import org.json.JSONObject

/**
 * * data class for tracking updates. This is received as params of a "tracking update" event (2).
 * Created by gabriel on 6/28/18.
 */

data class TrackingUpdate (val metadataKey: Long, val type: DeliveryTypes, val from: String) {
    companion object {

        fun fromJSON(jsonString: String): TrackingUpdate {
            val json = JSONObject(jsonString)
            return TrackingUpdate(
                    metadataKey = json.getLong("metadataKey"),
                    from = json.getString("from"),
                    type = DeliveryTypes.fromInt(json.getInt("type"))
            )
        }
    }
}