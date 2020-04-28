package com.criptext.mail.api.models

import org.json.JSONObject

data class PeerCustomDomainCreated(val domainName: String){
    companion object {
        fun fromJSON(jsonString: String): PeerCustomDomainCreated {
            val json = JSONObject(jsonString)
            return PeerCustomDomainCreated(
                    domainName = json.getString("customDomain")
            )
        }
    }
}