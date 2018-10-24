package com.criptext.mail.scenes.signin.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.scenes.settings.devices.DeviceItem
import com.criptext.mail.signal.PreKeyBundleShareData
import com.criptext.mail.utils.DeviceUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

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

    fun getFileStream(token: String, params: Map<String,String>): InputStream {
        return httpClient.getFileStream(path = "/userdata", authToken = token, params = params)
    }

    fun postLinkBegin(recipientId: String): String{
        val jsonPut = JSONObject()
        jsonPut.put("targetUsername", recipientId)

        return httpClient.post(path = "/link/begin", authToken = null, body = jsonPut)
    }

    fun getLinkStatus(jwt: String): String{
        return httpClient.get(path = "/link/status", authToken = jwt)
    }

    fun postLinkAuth(recipientId: String, jwt: String, device: DeviceItem, password: String?): String{
        val jsonPut = JSONObject()
        jsonPut.put("recipientId", recipientId)
        jsonPut.put("deviceName", device.name)
        jsonPut.put("deviceFriendlyName", device.friendlyName)
        jsonPut.put("deviceType", device.deviceType)
        jsonPut.put("password", password)

        return httpClient.post(path = "/link/auth", authToken = jwt, body = jsonPut)
    }

    fun isLinkDataReady(token: String): String {
        return httpClient.get("/link/data/ready", authToken = token)
    }

    fun acknowledgeEvents(eventIds: List<Long>, token: String): String {
        val jsonObject = JSONObject()
        jsonObject.put("ids", JSONArray(eventIds))

        return httpClient.post(authToken = token, path = "/event/ack", body = jsonObject)
    }

}
