package com.criptext.mail.scenes.settings.custom_domain_entry.data

import com.criptext.mail.api.CriptextAPIClient
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpResponseData
import org.json.JSONObject


class CustomDomainEntryAPIClient(private val httpClient: HttpClient, var token: String): CriptextAPIClient(httpClient) {

    fun postDomainExist(domain: String): HttpResponseData {
        val json = JSONObject()
        val dom = JSONObject()
        json.put("name", domain)
        dom.put("domain", json)
        return httpClient.post(path = "/domain/exist", authToken = token, body = dom)
    }

    fun postRegisterDomain(domain: String): HttpResponseData {
        val json = JSONObject()
        json.put("customDomain", domain)
        return httpClient.post(path = "/user/customdomain", authToken = token, body = json)
    }
}
