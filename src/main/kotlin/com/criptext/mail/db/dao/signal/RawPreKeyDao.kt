package com.criptext.mail.db.dao.signal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.criptext.mail.db.models.signal.CRPreKey

/**
 * Created by gabriel on 3/5/18.
 */

@Dao
interface RawPreKeyDao {

    @Query("DELETE from raw_prekey where id = :id")
    fun deleteById(id: Int)


    @Insert
    fun insertAll(prekeys : List<CRPreKey>)

    @Query("DELETE from raw_prekey")
    fun deleteAll()

    @Query("SELECT * from raw_prekey where id = :id")
    fun find(id: Int): CRPreKey?

    @Query("SELECT * from raw_prekey")
    fun getAll(): List<CRPreKey>

    @Insert
    fun insert(crPreKey: CRPreKey)
}