package com.email.signal

import com.email.db.models.signal.CRPreKey
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.SignedPreKeyRecord
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore

/**
 * Created by gabriel on 3/17/18.
 */
class InMemoryUser(recipientId: String, deviceId: Int) {
        private val generator = SignalKeyGenerator.Default()
        private val registrationBundles = generator.register(recipientId, deviceId)
        private val store = createStoreFromRegistrationBundle(registrationBundles)
        private val client = SignalClient.Default(store)

        init {
            val privateBundle = registrationBundles.privateBundle
            // insert pre keys
            privateBundle.preKeys.forEach { id, byteString ->
                val preKey = PreKeyRecord(Encoding.stringToByteArray(byteString))
                store.storePreKey(id, preKey)
            }

            // insert signed pre key
            val signedPreKeyRecord = SignedPreKeyRecord(
                    Encoding.stringToByteArray(privateBundle.signedPreKey))
            store.storeSignedPreKey(privateBundle.signedPreKeyId, signedPreKeyRecord)
        }

        fun fetchAPreKeyBundle(): PreKeyBundleShareData.DownloadBundle {
            val bundle = registrationBundles.uploadBundle
            val preKeyPublic = bundle.preKeys[0]!!
            val preKeyRecord = CRPreKey(0, preKeyPublic)

            return PreKeyBundleShareData.DownloadBundle(
                    shareData = registrationBundles.uploadBundle.shareData,
                    preKey = preKeyRecord)
        }

        fun buildSession(downloadBundle: PreKeyBundleShareData.DownloadBundle) {
            client.createSessionsFromBundles(listOf(downloadBundle))
        }

        fun encrypt(recipientId: String, deviceId: Int, text: String) =
            client.encryptMessage(recipientId, deviceId, text)

        fun decrypt(recipientId: String, deviceId: Int, text: String) =
                client.decryptMessage(recipientId, deviceId, text)

        private companion object {
            fun createStoreFromRegistrationBundle(registrationBundles: SignalKeyGenerator.RegistrationBundles)
                    : InMemorySignalProtocolStore {
                val privateBundle = registrationBundles.privateBundle
                val identityKeyPairBytes = Encoding.stringToByteArray(privateBundle.identityKeyPair)
                val identityKeyPair = IdentityKeyPair(identityKeyPairBytes)
                return InMemorySignalProtocolStore(identityKeyPair, privateBundle.registrationId)
            }
        }
    }