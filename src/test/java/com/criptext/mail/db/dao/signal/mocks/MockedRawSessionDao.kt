package com.criptext.mail.db.dao.signal.mocks

import com.criptext.mail.db.dao.signal.RawSessionDao
import com.criptext.mail.db.models.KnownAddress
import com.criptext.mail.db.models.signal.CRSessionRecord

/**
 * Created by gabriel on 4/5/18.
 */
class MockedRawSessionDao: RawSessionDao {
    override fun insert(crSessionRecord: CRSessionRecord) {
    }

    override fun delete(crSessionRecord: CRSessionRecord) {
    }

    override fun find(recipientId: String, deviceId: Int): CRSessionRecord? {
        TODO("implement")
    }

    override fun getKnownAddresses(recipients: List<String>): List<KnownAddress> {
        TODO("implement")
    }

    override fun deleteByRecipientId(recipientId: String) {
    }

    override fun findActiveDevicesByRecipientId(recipientId: String): List<Int> {
        TODO("implement")
    }

}