package com.criptext.mail.scenes.settings.custom_domain_entry.domainconfiguration.data

import com.criptext.mail.api.CriptextAPIClient
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpResponseData
import org.json.JSONObject


class DomainConfigurationAPIClient(private val httpClient: HttpClient, var token: String): CriptextAPIClient(httpClient) {

    fun getMXRecords(domain: String): HttpResponseData {
        return httpClient.get(path = "/domain/mx/$domain", authToken = token)
    }

    fun postValidateDomainMX(domain: String): HttpResponseData {
        val json = JSONObject()
        json.put("customDomain", domain)
        return httpClient.post(path = "/domain/validate/mx", authToken = token, body = json)
    }
}
