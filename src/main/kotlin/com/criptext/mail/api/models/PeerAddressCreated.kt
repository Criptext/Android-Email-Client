package com.criptext.mail.api.models

import org.json.JSONObject

data class PeerAddressCreated(val addressId: Long, val addressName: String, val addressDomain: String){
    companion object {
        fun fromJSON(jsonString: String): PeerAddressCreated {
            val json = JSONObject(jsonString)
            return PeerAddressCreated(
                    addressId = json.getLong("addressId"),
                    addressName = json.getString("addressName"),
                    addressDomain = json.getString("addressDomain")
            )
        }
    }
}