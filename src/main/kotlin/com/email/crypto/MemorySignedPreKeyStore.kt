package com.signaltest.crypto

import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.state.SignedPreKeyRecord
import org.whispersystems.libsignal.state.SignedPreKeyStore

/**
 * Created by gabriel on 11/7/17.
 */

class MemorySignedPreKeyStore : SignedPreKeyStore {
    private val keyMap = HashMap<Int, SignedPreKeyRecord>()

    override fun containsSignedPreKey(signedPreKeyId: Int): Boolean {
        return keyMap.containsKey(signedPreKeyId)
    }

    override fun storeSignedPreKey(signedPreKeyId: Int, record: SignedPreKeyRecord) {
        keyMap[signedPreKeyId] = record
    }

    override fun removeSignedPreKey(signedPreKeyId: Int) {
        keyMap.remove(signedPreKeyId)
    }

    override fun loadSignedPreKey(signedPreKeyId: Int): SignedPreKeyRecord {
        return keyMap[signedPreKeyId] ?: throw InvalidKeyIdException("No key for $signedPreKeyId")
    }

    override fun loadSignedPreKeys(): MutableList<SignedPreKeyRecord> {
        return keyMap.values.toMutableList()
    }

}