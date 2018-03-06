package com.email.api

import com.email.signal.Encoding
import org.json.JSONArray
import org.json.JSONObject
import org.whispersystems.libsignal.util.KeyHelper
import java.util.*
import kotlin.math.sign

/**
 * Created by gabriel on 11/10/17.
 */

data class PreKeyBundleShareData(val registrationId: Int,
                                 val deviceId: Int,
                                 val signedPreKeyId: Int,
                                 val signedPreKeyPublic: String,
                                 val signedPreKeySignature: String,
                                 val identityPublicKey: String,
                                 val identityKeyPair: String,
                                 val signedPrekey: String
                                 ) {

    data class DownloadBundle(val shareData: PreKeyBundleShareData, val preKeyId: Int,
                              val publicPreKey: String) {

        companion object {
            fun fromJSON(json: JSONObject): DownloadBundle {
                val preKey = json.getJSONObject("preKey")
                val signedPrekey = json.getString("signedPreKey")

                val registrationId = json.getInt("registrationId")
                val deviceId = json.getInt("deviceId")
                val recipientId = json.getString("recipientId")
                val signedPreKeyId = json.getInt("signedPreKeyId")
                val signedPreKeyPublic = json.getString("signedPreKeyPublic")
                val identityPublicKey = json.getString("identityPublicKey")
                val signedPreKeySignature = json.getString("signedPreKeySignature")
                val identityKeyPair = json.getString("identityKeyPair")

                val preKeyId = preKey.getInt("id")
                val publicPreKey = preKey.getString("publicKey")

                val shareData = PreKeyBundleShareData(
                        registrationId = registrationId,
                        deviceId = deviceId, signedPreKeyId = signedPreKeyId,
                        signedPreKeyPublic = signedPreKeyPublic,
                        signedPreKeySignature = signedPreKeySignature,
                        identityPublicKey = identityPublicKey,
                        identityKeyPair = identityKeyPair,
                        signedPrekey = signedPrekey)

                return DownloadBundle(shareData, preKeyId, publicPreKey)
            }
        }
    }

    data class UploadBundle(val shareData: PreKeyBundleShareData,
                            val serializedPreKeys: Map<Int, String>
    ) {
        fun toJSON(): JSONObject {
            val preKeyArray = JSONArray()
            serializedPreKeys.forEach { (id, key) ->
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
            keyBundle.put("signedPreKeySignature", shareData.signedPreKeySignature)
            keyBundle.put("preKeys", preKeyArray)

            return keyBundle
        }

        companion object {
            fun createKeyBundle(deviceId: Int):
                    PreKeyBundleShareData.UploadBundle {
                val random = Random()
                val identityKeyPair = KeyHelper.generateIdentityKeyPair()
                val signedPreKeyId = random.nextInt(99) + 1
                val signedPrekey = KeyHelper.generateSignedPreKey(identityKeyPair, signedPreKeyId)
                val shareData = PreKeyBundleShareData(
                        deviceId = deviceId,
                        signedPreKeyId = signedPreKeyId,
                        registrationId = KeyHelper.generateRegistrationId(false) ,
                        identityPublicKey = Encoding.byteArrayToString(identityKeyPair.publicKey.serialize()),
                        signedPreKeyPublic = Encoding.byteArrayToString(signedPrekey.serialize()),
                        signedPreKeySignature = Encoding.byteArrayToString(signedPrekey.signature),
                        identityKeyPair = Encoding.byteArrayToString(identityKeyPair.serialize()),
                        signedPrekey = Encoding.byteArrayToString(signedPrekey.serialize())
                )
                val preKeys = KeyHelper.generatePreKeys(0, 500)
                val serializedPrekeys = preKeys.map {
                    it.id to Encoding.byteArrayToString(
                            it.keyPair.publicKey.serialize())
                }.toMap()
                return PreKeyBundleShareData.UploadBundle(shareData,
                        serializedPrekeys)
            }
        }
    }
}