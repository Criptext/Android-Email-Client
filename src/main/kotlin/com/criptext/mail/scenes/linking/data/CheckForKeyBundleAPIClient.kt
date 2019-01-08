package com.criptext.mail.scenes.linking.data

import com.criptext.mail.api.CriptextAPIClient
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpResponseData
import org.json.JSONObject


class CheckForKeyBundleAPIClient(private val httpClient: HttpClient, var token: String): CriptextAPIClient(httpClient) {

    fun getKeyBundle(deviceId: Int): HttpResponseData {
        return httpClient.get(path = "/keybundle/$deviceId", authToken = token)
    }
}
