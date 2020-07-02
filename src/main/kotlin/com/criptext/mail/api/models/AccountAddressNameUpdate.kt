package com.criptext.mail.api.models

import org.json.JSONObject

data class AccountAddressNameUpdate(val recipientId: String, val domain: String,
                                    val addressId: Long, val fullName: String){
    companion object {
        fun fromJSON(jsonString: String): AccountAddressNameUpdate {
            val json = JSONObject(jsonString)
            return AccountAddressNameUpdate(
                    recipientId = json.getString("recipientId"),
                    domain = json.getString("recipientId"),
                    addressId = json.getLong("addressId"),
                    fullName = json.getString("fullName")
            )
        }
    }
}