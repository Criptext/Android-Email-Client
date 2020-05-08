package com.criptext.mail.api.models

import com.criptext.mail.db.AccountTypes
import org.json.JSONObject

data class AccountCustomerTypeChanged(val recipientId: String, val domain: String, val newType: AccountTypes){
    companion object {
        fun fromJSON(jsonString: String): AccountCustomerTypeChanged {
            val json = JSONObject(jsonString)
            return AccountCustomerTypeChanged(
                    recipientId = json.getString("recipientId"),
                    domain = json.getString("domain"),
                    newType = AccountTypes.fromInt(json.getInt("newCustomerType"))
            )
        }
    }
}