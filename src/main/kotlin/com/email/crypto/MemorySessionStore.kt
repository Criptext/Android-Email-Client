package com.signaltest.crypto

import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.SessionRecord
import org.whispersystems.libsignal.state.SessionStore

/**
 * Created by gabriel on 11/7/17.
 */

class MemorySessionStore : SessionStore {
    private val sessionMap = HashMap<SignalProtocolAddress, SessionRecord>()

    override fun containsSession(address: SignalProtocolAddress): Boolean {
        return sessionMap.containsKey(address)
    }

    override fun getSubDeviceSessions(name: String): MutableList<Int> {
        return sessionMap.keys
                .filter { signalProtocolAddress ->  signalProtocolAddress.name == name }
                .map { signalProtocolAddress -> signalProtocolAddress.deviceId }
                .toMutableList()
    }

    override fun loadSession(address: SignalProtocolAddress): SessionRecord {
        val existingSession = sessionMap[address]
        return if (existingSession == null) {
            val newSession = SessionRecord()
            sessionMap[address] =  newSession
            newSession
        } else
            existingSession
    }

    override fun deleteSession(address: SignalProtocolAddress) {
        sessionMap.remove(address)
    }

    override fun deleteAllSessions(name: String) {
        sessionMap.keys
            .filter { signalProtocolAddress -> signalProtocolAddress.name ==name }
            .forEach { signalProtocolAddress -> sessionMap.remove(signalProtocolAddress) }
    }

    override fun storeSession(address: SignalProtocolAddress, record: SessionRecord) {
        sessionMap[address] = record
    }
}