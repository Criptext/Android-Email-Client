package com.criptext.mail.scenes.composer.data

import com.criptext.mail.api.CriptextAPIClient
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpResponseData
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by gabriel on 3/15/18.
 */

class ComposerAPIClient(private val httpClient: HttpClient, var token: String): CriptextAPIClient(httpClient) {

    fun findKeyBundles(recipients: List<String>, knownAddresses: Map<String, List<Int>>): HttpResponseData {
        val jsonObject = FindKeybundleData.toJSON(recipients, knownAddresses)
        return httpClient.post(path = "/keybundle/find", authToken = token, body = jsonObject)
    }

    fun postEmail(postEmailBody: PostEmailBody): HttpResponseData {
        return httpClient.post(path = "/email", authToken = token, body = postEmailBody.toJSON())
    }

    fun duplicateAttachments(fileTokens: List<String>): HttpResponseData{
        val jsonObject = JSONObject()
        val fileTokenArray = JSONArray(fileTokens)
        jsonObject.put("files", fileTokenArray)
        return httpClient.post(path = "/file/duplicate", authToken = token, body = jsonObject)
    }

    fun getIsSecureDomain(domainList: List<String>): HttpResponseData {
        return httpClient.get(path = "/domain?${domainList.joinToString(separator = "&") { "domain=$it" }}", authToken = token)
    }

}
