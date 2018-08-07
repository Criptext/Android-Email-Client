package com.criptext.mail.services.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.signal.PreKeyBundleShareData
import org.json.JSONObject

/**
 * Created by sebas on 2/28/18.
 */

class MessagingServiceAPIClient(private val httpClient: HttpClient) {


    fun putFirebaseToken(pushToken: String, jwt: String): String {
        val jsonPut = JSONObject()
        jsonPut.put("devicePushToken", pushToken)

        return httpClient.put(path = "/keybundle/pushtoken", authToken = jwt, body = jsonPut)
    }
}
