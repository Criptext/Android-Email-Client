package com.criptext.mail.scenes.settings.aliases.data

import com.criptext.mail.api.CriptextAPIClient
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpResponseData
import org.json.JSONObject


class AliasesAPIClient(private val httpClient: HttpClient, var token: String): CriptextAPIClient(httpClient) {

    fun postAddAlias(alias: String, domain: String): HttpResponseData {
        val dom = JSONObject()
        dom.put("addressName", alias)
        dom.put("addressDomain", domain)
        return httpClient.post(path = "/user/address", authToken = token, body = dom)
    }

    fun deleteAlias(aliasRowId: Long): HttpResponseData {
        val dom = JSONObject()
        dom.put("addressId", aliasRowId)
        return httpClient.delete(path = "/user/address", authToken = token, body = dom)
    }

    fun putAliasActive(aliasKey: Long, enable: Boolean): HttpResponseData {
        val dom = JSONObject()
        dom.put("addressKey", aliasKey)
        dom.put("activate", enable)
        return httpClient.put(path = "/user/address/activate", authToken = token, body = dom)
    }
}
