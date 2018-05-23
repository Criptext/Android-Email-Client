package com.email.scenes.signin.data

import com.email.api.HttpClient
import com.email.signal.PreKeyBundleShareData
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
        return httpClient.post(path = "/user/auth", body = jsonObject, jwt = null)
    }

    fun postKeybundle(bundle: PreKeyBundleShareData.UploadBundle, jwt: String): String {
        return httpClient.post(path = "/keybundle", body = bundle.toJSON(), jwt = jwt)
    }
}
