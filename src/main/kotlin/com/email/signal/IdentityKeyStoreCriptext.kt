package com.email.signal

import com.email.db.DAO.UserDao
import com.email.db.dao.RawIdentityKeyDao
import com.email.db.models.User
import com.email.db.models.signal.RawIdentityKey
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.IdentityKeyStore

/**
 * Created by gabriel on 3/5/18.
 */

class IdentityKeyStoreCriptext(private val userDao: UserDao,
                               private val rawIdentityKeyDao: RawIdentityKeyDao): IdentityKeyStore {

    private fun getLoggedInUser(): User {
        val user = userDao.getLoggedInUser()
        return user ?: throw Exception("Please Log In")
    }

    override fun saveIdentity(address: SignalProtocolAddress, identityKey: IdentityKey) {
        val identityKeySerialized = Encoding.byteArrayToString(identityKey.serialize())
        val newIdentityKey = RawIdentityKey(recipientId = address.name, deviceId = address.deviceId,
                identityKey = identityKeySerialized)
        rawIdentityKeyDao.insert(newIdentityKey)
    }

    override fun getIdentityKeyPair(): IdentityKeyPair {
        val rawIdentityKeyPair = getLoggedInUser().rawIdentityKeyPair
        val bytesIdentityKeyPair = Encoding.stringToByteArray(rawIdentityKeyPair)
        return IdentityKeyPair(bytesIdentityKeyPair)
    }

    override fun isTrustedIdentity(address: SignalProtocolAddress, identityKey: IdentityKey): Boolean {
        val foundRawIdentity = rawIdentityKeyDao.find(recipientId = address.name, deviceId =
                               address.deviceId) ?: return true
        val identityKeyBytes = Encoding.stringToByteArray(foundRawIdentity.identityKey)
        val existingIdentity = IdentityKey(identityKeyBytes, 0)
        return identityKey == existingIdentity
    }

    override fun getLocalRegistrationId(): Int =
        getLoggedInUser().registrationId

}