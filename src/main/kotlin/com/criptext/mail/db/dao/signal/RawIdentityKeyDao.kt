package com.criptext.mail.db.dao.signal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.criptext.mail.db.models.signal.CRIdentityKey

/**
 * Created by gabriel on 3/6/18.
 */

@Dao
interface RawIdentityKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(crIdentityKey: CRIdentityKey)

    @Query("""SELECT * FROM raw_identitykey
              WHERE recipientId = :recipientId AND deviceId = :deviceId AND accountId = :accountId LIMIT 1""")
    fun find(recipientId: String, deviceId: Int, accountId: Long): CRIdentityKey?

    @Query("""DELETE FROM raw_identitykey
              WHERE recipientId = :recipientId AND accountId = :accountId""")
    fun deleteByRecipientId(recipientId: String, accountId: Long)

    @Query("DELETE FROM raw_identitykey WHERE accountId = :accountId")
    fun deleteAll(accountId: Long)

}