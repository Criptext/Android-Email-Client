package com.email.scenes.emailDetail.mocks

import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.SessionRecord
import org.whispersystems.libsignal.state.SignalProtocolStore
import org.whispersystems.libsignal.state.SignedPreKeyRecord

/**
 * Created by sebas on 3/29/18.
 */
class MockedSignalProtocolStore(): SignalProtocolStore {
    override fun saveIdentity(address: SignalProtocolAddress?, identityKey: IdentityKey?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun containsSignedPreKey(signedPreKeyId: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getIdentityKeyPair(): IdentityKeyPair {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSubDeviceSessions(name: String?): MutableList<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isTrustedIdentity(address: SignalProtocolAddress?, identityKey: IdentityKey?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun storeSignedPreKey(signedPreKeyId: Int, record: SignedPreKeyRecord?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun storePreKey(preKeyId: Int, record: PreKeyRecord?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteAllSessions(name: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLocalRegistrationId(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun containsPreKey(preKeyId: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun containsSession(address: SignalProtocolAddress?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun loadSession(address: SignalProtocolAddress?): SessionRecord {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteSession(address: SignalProtocolAddress?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeSignedPreKey(signedPreKeyId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removePreKey(preKeyId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun storeSession(address: SignalProtocolAddress?, record: SessionRecord?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun loadPreKey(preKeyId: Int): PreKeyRecord {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun loadSignedPreKey(signedPreKeyId: Int): SignedPreKeyRecord {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun loadSignedPreKeys(): MutableList<SignedPreKeyRecord> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
