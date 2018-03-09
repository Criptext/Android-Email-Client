package com.email.signal

import com.email.db.AppDatabase
import com.email.db.DAO.UserDao
import com.email.db.dao.RawIdentityKeyDao
import com.email.db.dao.RawPreKeyDao
import com.email.db.dao.RawSessionDao
import com.email.db.dao.RawSignedPreKeyDao
import com.email.db.models.User
import com.email.db.models.signal.CRIdentityKey
import com.email.db.models.signal.CRPreKey
import com.email.db.models.signal.CRSessionRecord
import com.email.db.models.signal.CRSignedPreKey
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.*

/**
 * Created by gabriel on 3/6/18.
 */

class SignalStoreCriptext(rawSessionDao: RawSessionDao, rawIdentityKeyDao: RawIdentityKeyDao,
                          userDao: UserDao, rawSignedPreKeyDao: RawSignedPreKeyDao,
                          rawPreKeyDao: RawPreKeyDao): SignalProtocolStore {

    constructor(db: AppDatabase): this(rawSessionDao = db.rawSessionDao(),
            rawIdentityKeyDao = db.rawIdentityKeyDao(), userDao = db.userDao(),
            rawSignedPreKeyDao = db.rawSignedPreKeyDao(),
            rawPreKeyDao = db.rawPreKeyDao())
    
    private val sessionStore = SessionStoreImplementation(rawSessionDao)
    private val identityKeyStore = IdentityKeyStoreImplementation(userDao = userDao,
            rawIdentityKeyDao = rawIdentityKeyDao)
    private val signedPreKeyStore = SignedPreKeyStoreImplementation(rawSignedPreKeyDao)
    private val preKeyStore = PreKeyStoreImplementation(rawPreKeyDao)

    override fun saveIdentity(address: SignalProtocolAddress, identityKey: IdentityKey) =
            identityKeyStore.saveIdentity(address, identityKey)

    override fun getIdentityKeyPair() = identityKeyStore.identityKeyPair

    override fun getLocalRegistrationId() = identityKeyStore.localRegistrationId

    override fun isTrustedIdentity(address: SignalProtocolAddress, identityKey: IdentityKey) =
            identityKeyStore.isTrustedIdentity(address, identityKey)

    override fun containsSignedPreKey(signedPreKeyId: Int) =
            signedPreKeyStore.containsSignedPreKey(signedPreKeyId)

    override fun storeSignedPreKey(signedPreKeyId: Int, record: SignedPreKeyRecord) =
            signedPreKeyStore.storeSignedPreKey(signedPreKeyId, record)

    override fun removeSignedPreKey(signedPreKeyId: Int) =
            signedPreKeyStore.removeSignedPreKey(signedPreKeyId)

    override fun loadSignedPreKey(signedPreKeyId: Int) =
            signedPreKeyStore.loadSignedPreKey(signedPreKeyId)

    override fun loadSignedPreKeys() = signedPreKeyStore.loadSignedPreKeys()

    override fun deleteAllSessions(name: String) = sessionStore.deleteAllSessions(name)

    override fun getSubDeviceSessions(name: String) = sessionStore.getSubDeviceSessions(name)

    override fun containsSession(address: SignalProtocolAddress) =
            sessionStore.containsSession(address)

    override fun loadSession(address: SignalProtocolAddress) = sessionStore.loadSession(address)

    override fun deleteSession(address: SignalProtocolAddress) = sessionStore.deleteSession(address)

    override fun storeSession(address: SignalProtocolAddress, record: SessionRecord)
            = sessionStore.storeSession(address, record)

    override fun storePreKey(preKeyId: Int, record: PreKeyRecord) =
            preKeyStore.storePreKey(preKeyId, record)

    override fun containsPreKey(preKeyId: Int) = preKeyStore.containsPreKey(preKeyId)

    override fun removePreKey(preKeyId: Int) = preKeyStore.removePreKey(preKeyId)

    override fun loadPreKey(preKeyId: Int) = preKeyStore.loadPreKey(preKeyId)

    private class SessionStoreImplementation(private val db: RawSessionDao): SessionStore {

        private fun loadSessionFromDB(address: SignalProtocolAddress) =
            db.find(recipientId = address.name, deviceId = address.deviceId)

        private fun createSignalSessionRecord(crSessionRecord: CRSessionRecord): SessionRecord {
            val bytes = Encoding.stringToByteArray(crSessionRecord.byteString)
            return SessionRecord(bytes)
        }

        override fun containsSession(address: SignalProtocolAddress) =
            loadSessionFromDB(address) != null

        override fun getSubDeviceSessions(name: String): List<Int> =
            db.findActiveDevicesByRecipientId(name)

        override fun loadSession(address: SignalProtocolAddress): SessionRecord {
            val rawSession = loadSessionFromDB(address)
            return if (rawSession != null)
                createSignalSessionRecord(rawSession)
            else SessionRecord()
        }

        override fun deleteSession(address: SignalProtocolAddress) {
            val rawSession = db.find(recipientId = address.name, deviceId = address.deviceId)
            if (rawSession != null)
                db.delete(rawSession)
        }

        override fun deleteAllSessions(name: String) {
            db.deleteByRecipientId(name)
        }

        override fun storeSession(address: SignalProtocolAddress, record: SessionRecord) {
            val sessionRecord = Encoding.byteArrayToString(record.serialize())
            val newRawSession = CRSessionRecord(recipientId = address.name, deviceId = address.deviceId,
                    byteString = sessionRecord)
            db.insert(newRawSession)
        }

    }

    private class IdentityKeyStoreImplementation(private val userDao: UserDao,
                                                 private val rawIdentityKeyDao: RawIdentityKeyDao)
        : IdentityKeyStore {

        private fun getLoggedInUser(): User {
            val user = userDao.getLoggedInUser()
            return user ?: throw Exception("Please Log In")
        }

        override fun saveIdentity(address: SignalProtocolAddress, identityKey: IdentityKey) {
            val identityKeySerialized = Encoding.byteArrayToString(identityKey.serialize())
            val newIdentityKey = CRIdentityKey(recipientId = address.name,
                    deviceId = address.deviceId, byteString = identityKeySerialized)
            rawIdentityKeyDao.insert(newIdentityKey)
        }

        override fun getIdentityKeyPair(): IdentityKeyPair {
            val rawIdentityKeyPair = getLoggedInUser().rawIdentityKeyPair
            val bytesIdentityKeyPair = Encoding.stringToByteArray(rawIdentityKeyPair)
            return IdentityKeyPair(bytesIdentityKeyPair)
        }

        override fun isTrustedIdentity(address: SignalProtocolAddress, identityKey: IdentityKey)
                : Boolean {
            val foundRawIdentity = rawIdentityKeyDao.find(recipientId = address.name, deviceId =
                                   address.deviceId) ?: return true
            val identityKeyBytes = Encoding.stringToByteArray(foundRawIdentity.byteString)
            val existingIdentity = IdentityKey(identityKeyBytes, 0)
            return identityKey == existingIdentity
        }

        override fun getLocalRegistrationId(): Int =
            getLoggedInUser().registrationId

    }

    private class SignedPreKeyStoreImplementation(private val rawSignedPreKeyDao: RawSignedPreKeyDao)
        : SignedPreKeyStore {

        override fun containsSignedPreKey(signedPreKeyId: Int): Boolean =
            rawSignedPreKeyDao.find(signedPreKeyId) != null

        private val createSignedPreKeyRecord: (CRSignedPreKey) -> SignedPreKeyRecord = { rawSignedPreKey ->
            val bytes = Encoding.stringToByteArray(rawSignedPreKey.byteString)
            SignedPreKeyRecord(bytes)
        }

        override fun storeSignedPreKey(signedPreKeyId: Int, record: SignedPreKeyRecord) {
            val byteString = Encoding.byteArrayToString(record.serialize())
            val newRawSignedPreKey = CRSignedPreKey(id = signedPreKeyId, byteString = byteString)
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

    private class PreKeyStoreImplementation(private val rawPreKeyDao: RawPreKeyDao): PreKeyStore {
        override fun containsPreKey(preKeyId: Int): Boolean {
            return rawPreKeyDao.find(preKeyId) != null
        }

        override fun storePreKey(preKeyId: Int, record: PreKeyRecord) {
            val preKeyString = Encoding.byteArrayToString(record.serialize())
            val newPreKey = CRPreKey(id = preKeyId, byteString = preKeyString)
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

}