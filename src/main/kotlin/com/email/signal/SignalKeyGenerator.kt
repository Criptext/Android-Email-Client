package com.email.signal

import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.SignedPreKeyRecord
import org.whispersystems.libsignal.util.KeyHelper
import java.util.*

/**
 * Created by sebas on 3/6/18.
 */

interface SignalKeyGenerator {
    fun  register(recipientId: String, deviceId: Int) : RegistrationBundles

    class Default : SignalKeyGenerator {

        private fun serializePreKeyPairs(preKeys: List<PreKeyRecord>): Map<Int, String> {
            return preKeys.map { preKey ->
                val pairBytes = preKey.serialize()
                preKey.id to Encoding.byteArrayToString(pairBytes)
            }.toMap()
        }

        private fun serializePreKeyPublicKeys(preKeys: List<PreKeyRecord>): Map<Int, String> {
            return preKeys.map { preKey ->
                val publicKeyBytes = preKey.keyPair.publicKey.serialize()
                preKey.id to Encoding.byteArrayToString(publicKeyBytes)
            }.toMap()
        }

        private fun createPrivateBundle(registrationData: RegistrationData) = PrivateBundle(
            identityKeyPair = Encoding.byteArrayToString(registrationData.identityKeyPair.serialize()),
            signedPreKeyId = registrationData.signedPreKeyId,
            signedPreKey = Encoding.byteArrayToString(registrationData.signedPreKey.serialize()),
            preKeys = serializePreKeyPairs(registrationData.preKeys),
            registrationId = registrationData.registrationId
            )

        private fun createUploadBundle(recipientId: String, deviceId: Int,
                                       registrationData: RegistrationData)
                : PreKeyBundleShareData.UploadBundle {
            val shareData = PreKeyBundleShareData(
                        recipientId = recipientId,
                        deviceId = deviceId,
                        signedPreKeyId = registrationData.signedPreKeyId,
                        registrationId = registrationData.registrationId,
                        identityPublicKey = Encoding.byteArrayToString(
                                registrationData.identityKeyPair.publicKey.serialize()),
                        signedPreKeyPublic = Encoding.byteArrayToString(
                                registrationData.signedPreKey.keyPair.publicKey.serialize()),
                        signedPreKeySignature = Encoding.byteArrayToString(
                                registrationData.signedPreKey.signature)
                        )


            return PreKeyBundleShareData.UploadBundle(shareData = shareData,
                    preKeys = serializePreKeyPublicKeys(registrationData.preKeys))

        }

        override fun register(recipientId: String, deviceId: Int): RegistrationBundles {
            val registrationData = RegistrationData()
            val privateBundle = createPrivateBundle(registrationData)
            val uploadBundle = createUploadBundle(recipientId, deviceId, registrationData)

            return RegistrationBundles(privateBundle, uploadBundle)
        }
    }

    private class RegistrationData {
        val registrationId: Int = KeyHelper.generateRegistrationId(false)
        val identityKeyPair: IdentityKeyPair = KeyHelper.generateIdentityKeyPair()
        val signedPreKeyId: Int = Random().nextInt(99) + 1
        val signedPreKey: SignedPreKeyRecord = KeyHelper.generateSignedPreKey(identityKeyPair, signedPreKeyId)
        val preKeys: List<PreKeyRecord> = KeyHelper.generatePreKeys(1, 100)
    }

    class PrivateBundle(val identityKeyPair: String, val signedPreKeyId: Int,
                        val signedPreKey: String, val registrationId: Int,
                        val preKeys: Map<Int, String>)

    class RegistrationBundles(val privateBundle: PrivateBundle,
                              val uploadBundle: PreKeyBundleShareData.UploadBundle)
}
