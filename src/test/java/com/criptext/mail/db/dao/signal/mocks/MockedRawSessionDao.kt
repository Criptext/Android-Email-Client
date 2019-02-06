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

    override fun delete(recipientId: String, deviceId: Int, accountId: Long) {

    }

    override fun find(recipientId: String, deviceId: Int, accountId: Long): CRSessionRecord? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getKnownAddresses(recipients: List<String>, accountId: Long): List<KnownAddress> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteByRecipientId(recipientId: String, accountId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findActiveDevicesByRecipientId(recipientId: String, accountId: Long): List<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteAll(accountId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}