package com.criptext.mail.api.models

import org.json.JSONObject

data class PeerAddressStatusUpdated(val addressId: Long, val isActive: Boolean){
    companion object {
        fun fromJSON(jsonString: String): PeerAddressStatusUpdated {
            val json = JSONObject(jsonString)
            return PeerAddressStatusUpdated(
                    addressId = json.getLong("addressId"),
                    isActive = json.getInt("activate") == 1
            )
        }
    }
}