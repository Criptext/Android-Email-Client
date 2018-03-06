package com.email.signal

import com.email.db.dao.RawPreKeyDao
import com.email.db.models.signal.RawPreKey
import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.PreKeyStore

/**
 * Created by gabriel on 3/5/18.
 */

class PreKeyStoreCriptext(private val rawPreKeyDao: RawPreKeyDao): PreKeyStore {
    override fun containsPreKey(preKeyId: Int): Boolean {
        return rawPreKeyDao.find(preKeyId) != null
    }

    override fun storePreKey(preKeyId: Int, record: PreKeyRecord) {
        val preKeyString = Encoding.byteArrayToString(record.serialize())
        val newPreKey = RawPreKey(id = preKeyId, byteString = preKeyString)
        rawPreKeyDao.insert(newPreKey)
    }

    override fun removePreKey(preKeyId: Int) {
        rawPreKeyDao.deleteById(preKeyId)
    }

    override fun loadPreKey(preKeyId: Int): PreKeyRecord  {
        val rawPreKey = rawPreKeyDao.find(preKeyId)
        if (rawPreKey != null) {
            val serializedPreKey = Encoding.stringToByteArray(rawPreKey.byteString)
            return PreKeyRecord(serializedPreKey)
        }
        throw InvalidKeyIdException("Not found id = $preKeyId")
    }
}
