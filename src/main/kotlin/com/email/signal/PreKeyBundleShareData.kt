package com.email.signal

import com.email.db.models.signal.CRPreKey
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

        companion object {
            fun fromJSON(json: JSONObject): DownloadBundle {
                val registrationId = json.getInt("registrationId")
                val deviceId = json.getInt("deviceId")
                val recipientId = json.getString("recipientId")
                val signedPreKeyId = json.getInt("signedPreKeyId")
                val signedPreKeyPublic = json.getString("signedPreKeyPublic")
                val identityPublicKey = json.getString("identityPublicKey")
                val signedPreKeySignature = json.getString("signedPreKeySignature")

                val preKeyJson = json.optJSONObject("preKey")
                val preKey = if (preKeyJson != null)
                    CRPreKey(id = preKeyJson.getInt("id"),
                             byteString = preKeyJson.getString("publicKey"))
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

            fun fromJSONArray(jsonArray: JSONArray): List<DownloadBundle> {
                val length = jsonArray.length()
                return (0..(length-1))
                        .map {
                            val json = jsonArray.getJSONObject(it)
                            fromJSON(json)
                        }
            }
        }
}

    data class UploadBundle(val shareData: PreKeyBundleShareData,
                            val preKeys: Map<Int, String>
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

            return keyBundle
        }
    }
}