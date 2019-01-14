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

    @Query("DELETE from raw_signedprekey WHERE id = :id")
    fun deleteById(id: Int)

    @Insert
    fun insert(crSignedPreKey: CRSignedPreKey)

    @Query("SELECT * FROM raw_signedprekey WHERE id = :id LIMIT 1")
    fun find(id: Int): CRSignedPreKey?

    @Query("SELECT * FROM raw_signedprekey")
    fun findAll(): List<CRSignedPreKey>

    @Query("DELETE FROM raw_signedprekey")
    fun deleteAll()

}