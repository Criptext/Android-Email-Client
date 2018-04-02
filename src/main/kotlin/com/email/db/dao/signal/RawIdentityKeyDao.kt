package com.email.db.dao.signal

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.email.db.models.signal.CRIdentityKey

/**
 * Created by gabriel on 3/6/18.
 */

@Dao
interface RawIdentityKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(crIdentityKey: CRIdentityKey)

    @Query("""SELECT * FROM raw_identitykey
              WHERE recipientId = :recipientId AND deviceId = :deviceId LIMIT 1""")
    fun find(recipientId: String, deviceId: Int): CRIdentityKey?

}