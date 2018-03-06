package com.email.signal

import com.email.db.dao.RawSignedPreKeyDao
import com.email.db.models.signal.RawSignedPreKey
import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.state.SignedPreKeyRecord
import org.whispersystems.libsignal.state.SignedPreKeyStore

/**
 * Created by gabriel on 3/6/18.
 */

class SignedPreKeyStoreCriptext(private val rawSignedPreKeyDao: RawSignedPreKeyDao): SignedPreKeyStore {

    override fun containsSignedPreKey(signedPreKeyId: Int): Boolean =
        rawSignedPreKeyDao.find(signedPreKeyId) != null

    private val createSignedPreKeyRecord: (RawSignedPreKey) -> SignedPreKeyRecord = { rawSignedPreKey ->
        val bytes = Encoding.stringToByteArray(rawSignedPreKey.byteString)
        SignedPreKeyRecord(bytes)
    }

    override fun storeSignedPreKey(signedPreKeyId: Int, record: SignedPreKeyRecord) {
        val byteString = Encoding.byteArrayToString(record.serialize())
        val newRawSignedPreKey = RawSignedPreKey(id = signedPreKeyId, byteString = byteString)
        rawSignedPreKeyDao.insert(newRawSignedPreKey)
    }

    override fun removeSignedPreKey(signedPreKeyId: Int) {
        rawSignedPreKeyDao.deleteById(signedPreKeyId)
    }

    override fun loadSignedPreKey(signedPreKeyId: Int): SignedPreKeyRecord {
        val rawSignedPreKey = rawSignedPreKeyDao.find(signedPreKeyId)
               ?: throw InvalidKeyIdException("Can't find signedPreKeyId $signedPreKeyId")
        return createSignedPreKeyRecord(rawSignedPreKey)
    }

    override fun loadSignedPreKeys(): MutableList<SignedPreKeyRecord> =
        rawSignedPreKeyDao.findAll()
            .map(createSignedPreKeyRecord)
            .toMutableList()
}