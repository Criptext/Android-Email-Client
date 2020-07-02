package com.criptext.mail.api.models

import org.json.JSONObject

data class AccountDefaultAddressUpdate(val recipientId: String, val domain: String,
                                       val addressId: Long?){
    companion object {
        fun fromJSON(jsonString: String): AccountDefaultAddressUpdate {
            val json = JSONObject(jsonString)
            return AccountDefaultAddressUpdate(
                    recipientId = json.getString("recipientId"),
                    domain = json.getString("recipientId"),
                    addressId = if(json.has("addressId"))
                                    json.getLong("addressId")
                                else null
            )
        }
    }
}