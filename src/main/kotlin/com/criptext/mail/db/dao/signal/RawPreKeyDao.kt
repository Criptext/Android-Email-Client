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

    @Query("DELETE from raw_prekey where preKeyId = :id AND accountId = :accountId")
    fun deleteById(id: Int, accountId: Long)


    @Insert
    fun insertAll(prekeys : List<CRPreKey>)

    @Query("DELETE from raw_prekey WHERE accountId = :accountId")
    fun deleteAll(accountId: Long)

    @Query("SELECT * from raw_prekey where preKeyId = :id AND accountId = :accountId")
    fun find(id: Int, accountId: Long): CRPreKey?

    @Query("SELECT * from raw_prekey WHERE accountId = :accountId")
    fun getAll(accountId: Long): List<CRPreKey>

    @Insert
    fun insert(crPreKey: CRPreKey)
}