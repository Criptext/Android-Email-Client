package com.criptext.mail.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.criptext.mail.db.models.Open

/**
 * Created by sebas on 2/7/18.
 */

@Dao
interface OpenDao {

    @Insert
    fun insert(open : Open)

    @Insert
    fun insertAll(opens : List<Open>)

    @Query("SELECT * FROM open")
    fun getAll() : List<Open>

    @Delete
    fun deleteAll(opens: List<Open>)

}
