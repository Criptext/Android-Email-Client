package com.criptext.mail.scenes.settings.custom_domain.data

import com.criptext.mail.api.CriptextAPIClient
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpResponseData
import org.json.JSONObject


class CustomDomainAPIClient(private val httpClient: HttpClient, var token: String): CriptextAPIClient(httpClient) {

    fun deleteDomain(domain: String): HttpResponseData {
        val dom = JSONObject()
        dom.put("customDomain", domain)
        return httpClient.delete(path = "/user/customdomain", authToken = token, body = dom)
    }
}
