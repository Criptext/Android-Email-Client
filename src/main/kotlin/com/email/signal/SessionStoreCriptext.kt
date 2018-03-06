package com.email.signal

import com.email.db.dao.RawSessionDao
import com.email.db.models.signal.RawSession
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.SessionRecord
import org.whispersystems.libsignal.state.SessionStore

/**
 * Created by gabriel on 3/5/18.
 */

class SessionStoreCriptext(private val db: RawSessionDao): SessionStore {

    private fun loadSessionFromDB(address: SignalProtocolAddress) =
        db.find(recipientId = address.name, deviceId = address.deviceId)

    private fun createSignalSessionRecord(rawSession: RawSession): SessionRecord {
        val bytes = Encoding.stringToByteArray(rawSession.byteString)
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
        val newRawSession = RawSession(recipientId = address.name, deviceId = address.deviceId,
                byteString = sessionRecord)
        db.insert(newRawSession)
    }

}