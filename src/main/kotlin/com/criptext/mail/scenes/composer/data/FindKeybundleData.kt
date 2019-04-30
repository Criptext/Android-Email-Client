package com.criptext.mail.scenes.composer.data

import com.criptext.mail.utils.EmailAddressUtils
import org.json.JSONArray
import org.json.JSONObject

class FindKeybundleData{
    companion object {
        fun toJSON(recipients: List<String>, knownAddresses: Map<String, List<Int>>): JSONObject {
            val jsonObject = JSONObject()
            val domainList = recipients
                    .map { EmailAddressUtils.extractEmailAddressDomain(it) }
                    .distinct()
            val jsonDomainArray = JSONArray()
            domainList.forEach { domain ->
                val jsonDomain = JSONObject()
                jsonDomain.put("name", domain)
                jsonDomain.put("recipients", JSONArray(recipients
                        .filter { EmailAddressUtils.extractEmailAddressDomain(it) == domain }
                        .map { EmailAddressUtils.extractRecipientIdFromAddress(it, domain) }))
                jsonDomain.put("knownAddresses", JSONObject(knownAddresses.filterKeys { it.contains(domain) }
                        .mapKeys { EmailAddressUtils.extractRecipientIdFromAddress(it.key, domain) }))
                jsonDomainArray.put(jsonDomain)
            }
            jsonObject.put("domains", jsonDomainArray)
            return jsonObject
        }
    }
}