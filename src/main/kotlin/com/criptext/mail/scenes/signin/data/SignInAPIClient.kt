package com.criptext.mail.scenes.signin.data

import com.criptext.mail.api.CriptextAPIClient
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpResponseData
import com.criptext.mail.scenes.settings.devices.data.DeviceItem
import com.criptext.mail.signal.PreKeyBundleShareData
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

/**
 * Created by sebas on 2/28/18.
 */

class SignInAPIClient(private val httpClient: HttpClient): CriptextAPIClient(httpClient) {

    fun authenticateUser(userData: UserData)
            : HttpResponseData {
        val jsonObject = JSONObject()
        jsonObject.put("username", userData.username)
        jsonObject.put("domain", userData.domain)
        jsonObject.put("password", userData.password)
        return httpClient.post(path = "/user/auth", body = jsonObject, authToken = null)
    }

    fun postKeybundle(bundle: PreKeyBundleShareData.UploadBundle, jwt: String): HttpResponseData {
        return httpClient.post(path = "/keybundle", body = bundle.toJSON(), authToken = jwt)
    }

    fun putFirebaseToken(pushToken: String, jwt: String): HttpResponseData {
        val jsonPut = JSONObject()
        jsonPut.put("devicePushToken", pushToken)

        return httpClient.put(path = "/keybundle/pushtoken", authToken = jwt, body = jsonPut)
    }

    fun getFileStream(token: String, params: Map<String,String>): InputStream {
        return httpClient.getFileStream(path = "/userdata", authToken = token, params = params)
    }

    fun postLinkBegin(recipientId: String, domain: String): HttpResponseData{
        val jsonPut = JSONObject()
        jsonPut.put("targetUsername", recipientId)
        jsonPut.put("domain", domain)
        jsonPut.put("version", UserDataWriter.FILE_SYNC_VERSION)

        return httpClient.post(path = "/link/begin", authToken = null, body = jsonPut)
    }

    fun getLinkStatus(jwt: String): HttpResponseData{
        return httpClient.get(path = "/link/status", authToken = jwt)
    }

    fun postLinkAuth(recipientId: String, jwt: String, device: DeviceItem, password: String?, domain: String): HttpResponseData{
        val jsonPut = JSONObject()
        jsonPut.put("recipientId", recipientId)
        jsonPut.put("domain", domain)
        jsonPut.put("deviceName", device.name)
        jsonPut.put("deviceFriendlyName", device.friendlyName)
        jsonPut.put("deviceType", device.deviceType)
        jsonPut.put("password", password)

        return httpClient.post(path = "/link/auth", authToken = jwt, body = jsonPut)
    }

    fun isLinkDataReady(token: String): HttpResponseData {
        return httpClient.get("/link/data/ready", authToken = token)
    }

    fun acknowledgeEvents(eventIds: List<Long>, token: String): HttpResponseData {
        val jsonObject = JSONObject()
        jsonObject.put("ids", JSONArray(eventIds))

        return httpClient.post(authToken = token, path = "/event/ack", body = jsonObject)
    }

}
