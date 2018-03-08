package com.email.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.email.db.models.signal.CRSignedPreKey

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

}