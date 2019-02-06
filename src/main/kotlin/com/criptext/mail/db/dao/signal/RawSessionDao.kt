package com.criptext.mail.db.dao.signal

import androidx.room.*
import com.criptext.mail.db.models.KnownAddress
import com.criptext.mail.db.models.signal.CRSessionRecord

/**
 * Created by gabriel on 3/5/18.
 */

@Dao
interface RawSessionDao {

    @Insert
    fun insert(crSessionRecord: CRSessionRecord)

    @Query("""DELETE FROM raw_session
              WHERE recipientId = :recipientId AND deviceId = :deviceId AND accountId = :accountId""")
    fun delete(recipientId: String, deviceId: Int, accountId: Long)

    /**
     * Each time signal calls storeSession it should replace any existing value, which isn't
     * exactly supported by SQL so let's delete first and then insertIgnoringConflicts in a single transaction.
     */
    @Transaction
    fun store(crSessionRecord: CRSessionRecord) {
        delete(crSessionRecord.recipientId, crSessionRecord.deviceId, crSessionRecord.accountId)
        insert(crSessionRecord)
    }

    @Query("""SELECT * FROM raw_session
              WHERE recipientId = :recipientId AND deviceId = :deviceId AND accountId = :accountId LIMIT 1""")
    fun find(recipientId: String, deviceId: Int, accountId: Long): CRSessionRecord?

    @Query("""SELECT recipientId, deviceId FROM raw_session
              WHERE recipientId in (:recipients) AND accountId = :accountId""")
    fun getKnownAddresses(recipients: List<String>, accountId: Long): List<KnownAddress>
    @Query("""DELETE FROM raw_session
              WHERE recipientId = :recipientId AND accountId = :accountId""")
    fun deleteByRecipientId(recipientId: String, accountId: Long)

    @Query("""SELECT deviceId FROM raw_session
              WHERE recipientId = :recipientId AND accountId = :accountId""")
    fun findActiveDevicesByRecipientId(recipientId: String, accountId: Long): List<Int>

    @Query("DELETE FROM raw_session WHERE accountId = :accountId")
    fun deleteAll(accountId: Long)
}