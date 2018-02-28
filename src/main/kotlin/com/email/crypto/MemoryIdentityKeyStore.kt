package com.signaltest.crypto

import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.IdentityKeyStore

/**
 * Created by gabriel on 11/7/17.
 */

class MemoryIdentityKeyStore(private val mIdentityKeyPair: IdentityKeyPair): IdentityKeyStore {
    private var mIdentityKey: IdentityKey? = null
    var mLocalRegistrationId: Int = 0

    override fun saveIdentity(address: SignalProtocolAddress?, identityKey: IdentityKey?) {
        mIdentityKey = identityKey
    }

    override fun getIdentityKeyPair(): IdentityKeyPair {
        return mIdentityKeyPair
    }

    override fun isTrustedIdentity(address: SignalProtocolAddress?, identityKey: IdentityKey?): Boolean {
        return true
    }

    override fun getLocalRegistrationId(): Int {
        return mLocalRegistrationId
    }

}