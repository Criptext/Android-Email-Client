package com.email.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.email.db.models.signal.RawPreKey

/**
 * Created by gabriel on 3/5/18.
 */

@Dao
interface RawPreKeyDao {

    @Query("DELETE from raw_prekey where id = :id")
    fun deleteById(id: Int)

    @Query("SELECT * from raw_prekey where id = :id")
    fun find(id: Int): RawPreKey?

    @Insert
    fun insert(rawPreKey: RawPreKey)
}