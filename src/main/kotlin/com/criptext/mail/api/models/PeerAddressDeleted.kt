package com.criptext.mail.api.models

import org.json.JSONObject

data class PeerAddressDeleted(val addressId: Long){
    companion object {
        fun fromJSON(jsonString: String): PeerAddressDeleted {
            val json = JSONObject(jsonString)
            return PeerAddressDeleted(
                    addressId = json.getLong("addressId")
            )
        }
    }
}