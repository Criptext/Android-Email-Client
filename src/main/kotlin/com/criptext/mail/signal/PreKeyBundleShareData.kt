package com.criptext.mail.signal

import com.criptext.mail.db.models.signal.CRPreKey
import com.criptext.mail.utils.DeviceUtils
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

/**
 * Created by gabriel on 11/10/17.
 */

data class PreKeyBundleShareData(val recipientId: String,
                                 val registrationId: Int,
                                 val deviceId: Int,
                                 val signedPreKeyId: Int,
                                 val signedPreKeyPublic: String,
                                 val signedPreKeySignature: String,
                                 val identityPublicKey: String) {

    data class DownloadBundle(val shareData: PreKeyBundleShareData,
                              val preKey: CRPreKey?) {

        fun toJSON(): JSONObject {
            val json = JSONObject()
            json.put("registrationId", shareData.registrationId)
            json.put("deviceId", shareData.deviceId)
            json.put("recipientId", shareData.recipientId)
            json.put("signedPreKeyId", shareData.signedPreKeyId)
            json.put("signedPreKeyPublic", shareData.signedPreKeyPublic)
            json.put("identityPublicKey", shareData.identityPublicKey)
            json.put("signedPreKeySignature", shareData.signedPreKeySignature)

            if (preKey != null) {
                val preKeyJson = JSONObject()
                preKeyJson.put("id", preKey.preKeyId)
                preKeyJson.put("publicKey", preKey.byteString)

                json.put("preKey", preKeyJson)
            }

            return json
        }

        companion object {
            fun fromJSON(json: JSONObject, accountId: Long): DownloadBundle {
                val registrationId = json.getInt("registrationId")
                val deviceId = json.getInt("deviceId")
                val recipientId = json.getString("recipientId")
                val signedPreKeyId = json.getInt("signedPreKeyId")
                val signedPreKeyPublic = json.getString("signedPreKeyPublic")
                val identityPublicKey = json.getString("identityPublicKey")
                val signedPreKeySignature = json.getString("signedPreKeySignature")

                val preKeyJson = json.optJSONObject("preKey")
                val preKey = if (preKeyJson != null)
                    CRPreKey(id = 0,
                             preKeyId = preKeyJson.getInt("id"),
                             byteString = preKeyJson.getString("publicKey"),
                             accountId = accountId)
                    else null

                val shareData = PreKeyBundleShareData(
                        recipientId = recipientId,
                        registrationId = registrationId,
                        deviceId = deviceId, signedPreKeyId = signedPreKeyId,
                        signedPreKeyPublic = signedPreKeyPublic,
                        signedPreKeySignature = signedPreKeySignature,
                        identityPublicKey = identityPublicKey)

                return DownloadBundle(shareData = shareData, preKey = preKey)
            }

            fun fromJSONArray(jsonArray: JSONArray, accountId: Long): List<DownloadBundle> {
                val length = jsonArray.length()
                return (0..(length-1))
                        .map {
                            val json = jsonArray.getJSONObject(it)
                            fromJSON(json, accountId)
                        }
            }
        }
}

    data class UploadBundle(val shareData: PreKeyBundleShareData,
                            val preKeys: Map<Int, String>,
                            val deviceType: DeviceUtils.DeviceType,
                            val deviceFriendlyName: String,
                            val deviceName: String
    ) {
        fun toJSON(): JSONObject {
            val preKeyArray = JSONArray()
            preKeys.forEach { (id, key) ->
                val item = JSONObject()
                item.put("id", id)
                item.put("publicKey", key)
                preKeyArray.put(item)
            }
            val keyBundle = JSONObject()
            keyBundle.put("registrationId", shareData.registrationId)
            keyBundle.put("signedPreKeyId", shareData.signedPreKeyId)
            keyBundle.put("signedPreKeyPublic", shareData.signedPreKeyPublic)
            keyBundle.put("identityPublicKey", shareData.identityPublicKey)
            keyBundle.put("signedPreKeySignature",
                    shareData.signedPreKeySignature)
            keyBundle.put("preKeys", preKeyArray)
            keyBundle.put("deviceName", deviceName)
            keyBundle.put("deviceFriendlyName", deviceFriendlyName)
            keyBundle.put("deviceType", deviceType.ordinal)

            return keyBundle
        }
    }
}