package com.criptext.mail.signal

import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.signal.RawIdentityKeyDao
import com.criptext.mail.db.dao.signal.RawPreKeyDao
import com.criptext.mail.db.dao.signal.RawSessionDao
import com.criptext.mail.db.dao.signal.RawSignedPreKeyDao
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.signal.CRIdentityKey
import com.criptext.mail.db.models.signal.CRPreKey
import com.criptext.mail.db.models.signal.CRSessionRecord
import com.criptext.mail.db.models.signal.CRSignedPreKey
import com.criptext.mail.utils.Encoding
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.*

/**
 * Created by gabriel on 3/6/18.
 */

class SignalStoreCriptext(rawSessionDao: RawSessionDao, rawIdentityKeyDao: RawIdentityKeyDao,
                          accountDao: AccountDao, rawSignedPreKeyDao: RawSignedPreKeyDao,
                          rawPreKeyDao: RawPreKeyDao, activeAccount: ActiveAccount? = null): SignalProtocolStore {

    constructor(db: AppDatabase, activeAccount: ActiveAccount? = null): this(rawSessionDao = db.rawSessionDao(),
            rawIdentityKeyDao = db.rawIdentityKeyDao(), accountDao = db.accountDao(),
            rawSignedPreKeyDao = db.rawSignedPreKeyDao(),
            rawPreKeyDao = db.rawPreKeyDao(), activeAccount = activeAccount)
    
    private val sessionStore = SessionStoreImplementation(rawSessionDao, accountDao, activeAccount)
    private val identityKeyStore = IdentityKeyStoreImplementation(accountDao = accountDao,
            rawIdentityKeyDao = rawIdentityKeyDao, activeAccount = activeAccount)
    private val signedPreKeyStore = SignedPreKeyStoreImplementation(rawSignedPreKeyDao, accountDao, activeAccount)
    private val preKeyStore = PreKeyStoreImplementation(rawPreKeyDao, accountDao, activeAccount)

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

    private class SessionStoreImplementation(private val db: RawSessionDao, private val accountDao: AccountDao,
                                             private val activeAccount: ActiveAccount?): SessionStore {

        private val account by lazy { activeAccount ?: ActiveAccount
                .loadFromDB(accountDao.getLoggedInAccount()!!)!! }

        private fun loadSessionFromDB(address: SignalProtocolAddress) =
            db.find(recipientId = address.name, deviceId = address.deviceId, accountId = account.id)

        private fun createSignalSessionRecord(crSessionRecord: CRSessionRecord): SessionRecord {
            val bytes = Encoding.stringToByteArray(crSessionRecord.byteString)
            return SessionRecord(bytes)
        }

        override fun containsSession(address: SignalProtocolAddress) =
            loadSessionFromDB(address) != null

        override fun getSubDeviceSessions(name: String): List<Int> =
            db.findActiveDevicesByRecipientId(name, account.id)

        override fun loadSession(address: SignalProtocolAddress): SessionRecord {
            val rawSession = loadSessionFromDB(address)
            return if (rawSession != null)
                createSignalSessionRecord(rawSession)
            else SessionRecord()
        }

        override fun deleteSession(address: SignalProtocolAddress) {
            val rawSession = db.find(recipientId = address.name, deviceId = address.deviceId,
                    accountId = account.id)
            if (rawSession != null)
                db.delete(rawSession.recipientId, rawSession.deviceId, rawSession.accountId)
        }

        override fun deleteAllSessions(name: String) {
            db.deleteByRecipientId(name, account.id)
        }

        override fun storeSession(address: SignalProtocolAddress, record: SessionRecord) {
            val sessionRecord = Encoding.byteArrayToString(record.serialize())
            val newRawSessionValue = CRSessionRecord(0, recipientId = address.name, deviceId = address.deviceId,
                    byteString = sessionRecord, accountId = account.id)
            db.store(newRawSessionValue)
        }

    }

    private class IdentityKeyStoreImplementation(private val accountDao: AccountDao,
                                                 private val rawIdentityKeyDao: RawIdentityKeyDao,
                                                 private val activeAccount: ActiveAccount?)
        : IdentityKeyStore {

        private fun getLoggedInAccount(): Account {
            val user = if(activeAccount == null)
                accountDao.getLoggedInAccount()
            else
                accountDao.getAccountByRecipientId(activeAccount.recipientId)
            return user ?: throw Exception("Please Log In")
        }

        override fun saveIdentity(address: SignalProtocolAddress, identityKey: IdentityKey) {
            if(address.name == SignalUtils.externalRecipientId) return
            val identityKeySerialized = Encoding.byteArrayToString(identityKey.serialize())
            val newIdentityKey = CRIdentityKey(id = 0, recipientId = address.name,
                    deviceId = address.deviceId, byteString = identityKeySerialized, accountId = getLoggedInAccount().id)
            rawIdentityKeyDao.insert(newIdentityKey)
        }

        override fun getIdentityKeyPair(): IdentityKeyPair {
            val rawIdentityKeyPair = getLoggedInAccount().identityKeyPairB64
            val bytesIdentityKeyPair = Encoding.stringToByteArray(rawIdentityKeyPair)
            return IdentityKeyPair(bytesIdentityKeyPair)
        }

        override fun isTrustedIdentity(address: SignalProtocolAddress, identityKey: IdentityKey)
                : Boolean {
            if(address.name == SignalUtils.externalRecipientId) return true
            val foundRawIdentity = rawIdentityKeyDao.find(recipientId = address.name, deviceId =
                                   address.deviceId, accountId = getLoggedInAccount().id) ?: return true
            val identityKeyBytes = Encoding.stringToByteArray(foundRawIdentity.byteString)
            val existingIdentity = IdentityKey(identityKeyBytes, 0)
            return identityKey == existingIdentity
        }

        override fun getLocalRegistrationId(): Int =
            getLoggedInAccount().registrationId

    }

    private class SignedPreKeyStoreImplementation(private val rawSignedPreKeyDao: RawSignedPreKeyDao,
                                                  private val accountDao: AccountDao,
                                                  private val activeAccount: ActiveAccount?)
        : SignedPreKeyStore {

        private val account by lazy { activeAccount ?: ActiveAccount
                .loadFromDB(accountDao.getLoggedInAccount()!!)!! }

        override fun containsSignedPreKey(signedPreKeyId: Int): Boolean =
            rawSignedPreKeyDao.find(signedPreKeyId, account.id) != null

        private val createSignedPreKeyRecord: (CRSignedPreKey) -> SignedPreKeyRecord = { rawSignedPreKey ->
            val bytes = Encoding.stringToByteArray(rawSignedPreKey.byteString)
            SignedPreKeyRecord(bytes)
        }

        override fun storeSignedPreKey(signedPreKeyId: Int, record: SignedPreKeyRecord) {
            val byteString = Encoding.byteArrayToString(record.serialize())
            val newRawSignedPreKey = CRSignedPreKey(id = signedPreKeyId, byteString = byteString,
                    accountId = account.id)
            rawSignedPreKeyDao.insert(newRawSignedPreKey)
        }

        override fun removeSignedPreKey(signedPreKeyId: Int) {
            rawSignedPreKeyDao.deleteById(signedPreKeyId, account.id)
        }

        override fun loadSignedPreKey(signedPreKeyId: Int): SignedPreKeyRecord {
            val rawSignedPreKey = rawSignedPreKeyDao.find(signedPreKeyId, account.id)
                   ?: throw InvalidKeyIdException("Can't find signedPreKeyId $signedPreKeyId")
            return createSignedPreKeyRecord(rawSignedPreKey)
        }

        override fun loadSignedPreKeys(): MutableList<SignedPreKeyRecord> =
            rawSignedPreKeyDao.findAll(account.id)
                .map(createSignedPreKeyRecord)
                .toMutableList()
    }

    private class PreKeyStoreImplementation(private val rawPreKeyDao: RawPreKeyDao,
                                            private val accountDao: AccountDao,
                                            private val activeAccount: ActiveAccount?): PreKeyStore {

        private fun getLoggedInAccount(): Account {
            val user = if(activeAccount == null)
                accountDao.getLoggedInAccount()
            else
                accountDao.getAccountByRecipientId(activeAccount.recipientId)
            return user ?: throw Exception("Please Log In")
        }

        override fun containsPreKey(preKeyId: Int): Boolean {
            val account = getLoggedInAccount()
            return rawPreKeyDao.find(preKeyId, account.id) != null
        }

        override fun storePreKey(preKeyId: Int, record: PreKeyRecord) {
            val preKeyString = Encoding.byteArrayToString(record.serialize())
            val newPreKey = CRPreKey(id = 0, preKeyId = preKeyId, byteString = preKeyString,
                    accountId = getLoggedInAccount().id)
            rawPreKeyDao.insert(newPreKey)
        }

        override fun removePreKey(preKeyId: Int) {
            rawPreKeyDao.deleteById(preKeyId, getLoggedInAccount().id)
        }

        override fun loadPreKey(preKeyId: Int): PreKeyRecord  {
            val rawPreKey = rawPreKeyDao.find(preKeyId, getLoggedInAccount().id)
            if (rawPreKey != null) {
                val serializedPreKey = Encoding.stringToByteArray(rawPreKey.byteString)
                return PreKeyRecord(serializedPreKey)
            }
            throw InvalidKeyIdException("Not found id = $preKeyId")
        }
    }

}