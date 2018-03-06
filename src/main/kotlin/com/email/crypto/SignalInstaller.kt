package com.signaltest.crypto

import org.whispersystems.libsignal.state.PreKeyStore
import org.whispersystems.libsignal.state.SignedPreKeyStore
import org.whispersystems.libsignal.util.KeyHelper
import java.util.*
import kotlin.math.sign

/**
 * Created by gabriel on 11/7/17.
 */
object SignalInstaller {
    fun install(): SignalInstallData {
        val random = Random()
        val signedPrekeyId = random.nextInt(99) + 1
        val identityKeyPair = KeyHelper.generateIdentityKeyPair()
        return SignalInstallData(identityKeyPair,
                KeyHelper.generateRegistrationId(false),
                KeyHelper.generateSignedPreKey(identityKeyPair, signedPrekeyId),
                KeyHelper.generatePreKeys(0,100)
                )
    }

    fun storeInstallData(data: SignalInstallData, preKeyStore: PreKeyStore,
                         signedPreKeyStore: SignedPreKeyStore) {
        data.preKeyRecords.forEach { preKey ->
            preKeyStore.storePreKey(preKey.id, preKey)
        }
        val signedPreKey = data.identification.signedPreKeyRecord
        signedPreKeyStore.storeSignedPreKey(signedPreKey.id, signedPreKey)
    }
}