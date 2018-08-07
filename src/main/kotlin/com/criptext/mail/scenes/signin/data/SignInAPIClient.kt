package com.criptext.mail.scenes.signin.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.signal.PreKeyBundleShareData
import org.json.JSONObject

/**
 * Created by sebas on 2/28/18.
 */

class SignInAPIClient(private val httpClient: HttpClient) {

    fun authenticateUser(
            username: String,
            password: String)
            : String {
        val jsonObject = JSONObject()
        jsonObject.put("username", username)
        jsonObject.put("password", password)
        return httpClient.post(path = "/user/auth", body = jsonObject, authToken = null)
    }

    fun postKeybundle(bundle: PreKeyBundleShareData.UploadBundle, jwt: String): String {
        return httpClient.post(path = "/keybundle", body = bundle.toJSON(), authToken = jwt)
    }

    fun putFirebaseToken(pushToken: String, jwt: String): String {
        val jsonPut = JSONObject()
        jsonPut.put("devicePushToken", pushToken)

        return httpClient.put(path = "/keybundle/pushtoken", authToken = jwt, body = jsonPut)
    }
}
