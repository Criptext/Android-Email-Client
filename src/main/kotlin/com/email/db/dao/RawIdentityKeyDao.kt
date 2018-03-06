package com.email.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.email.db.models.signal.RawIdentityKey

/**
 * Created by gabriel on 3/6/18.
 */

@Dao
interface RawIdentityKeyDao {

    @Insert
    fun insert(rawIdentityKey: RawIdentityKey)

    @Query("""SELECT * FROM raw_identitykey
              WHERE recipientId = :recipientId AND deviceId = :deviceId LIMIT 1""")
    fun find(recipientId: String, deviceId: Int): RawIdentityKey?

}