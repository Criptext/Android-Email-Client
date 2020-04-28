package com.criptext.mail.api.models

import org.json.JSONObject

data class PeerCustomDomainDeleted(val domainName: String){
    companion object {
        fun fromJSON(jsonString: String): PeerCustomDomainDeleted {
            val json = JSONObject(jsonString)
            return PeerCustomDomainDeleted(
                    domainName = json.getString("customDomain")
            )
        }
    }
}