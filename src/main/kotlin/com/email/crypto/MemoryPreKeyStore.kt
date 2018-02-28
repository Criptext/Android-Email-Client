package com.signaltest.crypto

import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.PreKeyStore

/**
 * Created by gabriel on 11/7/17.
 */

class MemoryPreKeyStore : PreKeyStore {
    private val keyMap = HashMap<Int, PreKeyRecord>()
    override fun containsPreKey(preKeyId: Int): Boolean {
        return keyMap.containsKey(preKeyId)
    }

    override fun storePreKey(preKeyId: Int, record: PreKeyRecord) {
        keyMap[preKeyId] = record
    }

    override fun removePreKey(preKeyId: Int) {
        keyMap.remove(preKeyId)
    }

    override fun loadPreKey(preKeyId: Int): PreKeyRecord? {
        return keyMap[preKeyId]
    }

}