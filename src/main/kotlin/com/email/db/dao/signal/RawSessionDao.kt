package com.email.db.dao.signal

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.email.db.models.signal.CRSessionRecord

/**
 * Created by gabriel on 3/5/18.
 */

@Dao
interface RawSessionDao {

    @Insert
    fun insert(crSessionRecord: CRSessionRecord)

    @Delete
    fun delete(crSessionRecord: CRSessionRecord)

    @Query("""SELECT * FROM raw_session
              WHERE recipientId = :recipientId AND deviceId = :deviceId LIMIT 1""")
    fun find(recipientId: String, deviceId: Int): CRSessionRecord?

    @Query("""DELETE FROM raw_session
              WHERE recipientId = :recipientId""")
    fun deleteByRecipientId(recipientId: String)

    @Query("""SELECT deviceId FROM raw_session
              WHERE recipientId = :recipientId""")
    fun findActiveDevicesByRecipientId(recipientId: String): List<Int>
}