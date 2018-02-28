package com.signaltest.api

import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by gabriel on 11/10/17.
 */

data class PreKeyBundleShareData(val recipientId: String, val registrationId: Int, val deviceId: Int,
                                 val signedPreKeyId: Int, val signedPreKeyPublic: String,
                                 val signedPreKeySignature: String, val identityPublicKey: String) {

    data class DownloadBundle(val shareData: PreKeyBundleShareData, val preKeyId: Int,
                              val publicPreKey: String) {

        companion object {
            fun fromJSON(json: JSONObject): DownloadBundle {
                val preKey = json.getJSONObject("preKey")

                val registrationId = json.getInt("registrationId")
                val deviceId = json.getInt("deviceId")
                val recipientId = json.getString("recipientId")
                val signedPreKeyId = json.getInt("signedPreKeyId")
                val signedPreKeyPublic = json.getString("signedPreKeyPublic")
                val identityPublicKey = json.getString("identityPublicKey")
                val signedPreKeySignature = json.getString("signedPreKeySignature")

                val preKeyId = preKey.getInt("id")
                val publicPreKey = preKey.getString("publicKey")

                val shareData = PreKeyBundleShareData(recipientId = recipientId,
                        registrationId = registrationId,
                        deviceId = deviceId, signedPreKeyId = signedPreKeyId,
                        signedPreKeyPublic = signedPreKeyPublic,
                        signedPreKeySignature = signedPreKeySignature,
                        identityPublicKey = identityPublicKey)

                return DownloadBundle(shareData, preKeyId, publicPreKey)
            }
        }
    }

    data class UploadBundle(val shareData: PreKeyBundleShareData,
                            val serializedPreKeys: Map<Int, String>) {
        fun toJSON(): JSONObject {
            val preKeyArray = JSONArray()
            serializedPreKeys.forEach { (id, key) ->
                val item = JSONObject()
                item.put("id", id)
                item.put("publicKey", key)
                preKeyArray.put(item)
            }
            val keyBundle = JSONObject()
            keyBundle.put("recipientId", shareData.recipientId)
            keyBundle.put("registrationId", shareData.registrationId)
            keyBundle.put("deviceId", shareData.deviceId)
            keyBundle.put("signedPreKeyId", shareData.signedPreKeyId)
            keyBundle.put("signedPreKeyPublic", shareData.signedPreKeyPublic)
            keyBundle.put("identityPublicKey", shareData.identityPublicKey)
            keyBundle.put("signedPreKeySignature", shareData.signedPreKeySignature)
            keyBundle.put("preKeys", preKeyArray)

            return keyBundle
        }
    }
}