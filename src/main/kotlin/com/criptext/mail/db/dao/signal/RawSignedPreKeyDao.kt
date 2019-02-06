package com.criptext.mail.db.dao.signal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.criptext.mail.db.models.signal.CRSignedPreKey

/**
 * Created by gabriel on 3/6/18.
 */

@Dao
interface RawSignedPreKeyDao {

    @Query("DELETE from raw_signedprekey WHERE id = :id AND accountId = :accountId")
    fun deleteById(id: Int, accountId: Long)

    @Insert
    fun insert(crSignedPreKey: CRSignedPreKey)

    @Query("SELECT * FROM raw_signedprekey WHERE id = :id AND accountId = :accountId LIMIT 1")
    fun find(id: Int, accountId: Long): CRSignedPreKey?

    @Query("SELECT * FROM raw_signedprekey WHERE accountId = :accountId")
    fun findAll(accountId: Long): List<CRSignedPreKey>

    @Query("DELETE FROM raw_signedprekey WHERE accountId = :accountId")
    fun deleteAll(accountId: Long)

}