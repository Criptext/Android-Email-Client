package com.email.signal

import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.SignalProtocolStore
import org.whispersystems.libsignal.state.SignedPreKeyRecord
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore

/**
 * Created by gabriel on 3/17/18.
 */
class InMemoryUser(generator: SignalKeyGenerator, recipientId: String, deviceId: Int)
    : TestUser(generator, recipientId, deviceId) {

    override val store: SignalProtocolStore =
        createStoreFromRegistrationBundle(registrationBundles)

    init {
        val privateBundle = registrationBundles.privateBundle
        // insertIgnoringConflicts pre keys
        privateBundle.preKeys.forEach { (id , byteString) ->
            val preKey = PreKeyRecord(Encoding.stringToByteArray(byteString))
            store.storePreKey(id, preKey)
        }

        // insertIgnoringConflicts signed pre key
        val signedPreKeyRecord = SignedPreKeyRecord(
                Encoding.stringToByteArray(privateBundle.signedPreKey))
        store.storeSignedPreKey(privateBundle.signedPreKeyId, signedPreKeyRecord)
    }
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