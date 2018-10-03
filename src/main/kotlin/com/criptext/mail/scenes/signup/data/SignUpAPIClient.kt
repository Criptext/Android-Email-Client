package com.criptext.mail.scenes.signup.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.signal.PreKeyBundleShareData
import com.criptext.mail.scenes.signup.IncompleteAccount
import com.criptext.mail.utils.DeviceUtils
import org.json.JSONObject

/**
 * Created by sebas on 2/26/18.
 */

class SignUpAPIClient(private val httpClient: HttpClient) {

    fun createUser(
            account: IncompleteAccount,
            keyBundle : PreKeyBundleShareData.UploadBundle
    ): String {
        val jsonObject = JSONObject()
        jsonObject.put("name", account.name)
        jsonObject.put("password", account.password)
        jsonObject.put("recipientId", account.username)
        jsonObject.put("keybundle", keyBundle.toJSON())
        if (account.recoveryEmail != null)
            jsonObject.put("recoveryEmail", account.recoveryEmail)

        return httpClient.post("/user", null, jsonObject)
    }

    fun isUsernameAvailable(username: String): String {
        return httpClient.get("/user/available?username=$username", null)
    }

    fun postForgotPassword(recipientId: String): String{
        val jsonPut = JSONObject()
        jsonPut.put("recipientId", recipientId)

        return httpClient.post(path = "/user/password/reset", authToken = null, body = jsonPut)
    }

    fun putFirebaseToken(pushToken: String, jwt: String): String {
        val jsonPut = JSONObject()
        jsonPut.put("devicePushToken", pushToken)

        return httpClient.put(path = "/keybundle/pushtoken", authToken = jwt, body = jsonPut)
    }

    fun postLinkBegin(recipientId: String): String{
        val jsonPut = JSONObject()
        jsonPut.put("targetUsername", recipientId)

        return httpClient.post(path = "/link/begin", authToken = null, body = jsonPut)
    }

    fun postLinkStatus(jwt: String): String{
        return httpClient.post(path = "/link/status", authToken = jwt, body = JSONObject())
    }

    fun postLinkAuth(recipientId: String, jwt: String): String{
        val jsonPut = JSONObject()
        jsonPut.put("recipientId", recipientId)
        jsonPut.put("deviceName", DeviceUtils.getDeviceName())
        jsonPut.put("deviceFriendlyName", DeviceUtils.getDeviceFriendlyName())
        jsonPut.put("deviceType", DeviceUtils.getDeviceType().ordinal)

        return httpClient.post(path = "/link/auth", authToken = jwt, body = jsonPut)
    }
}
