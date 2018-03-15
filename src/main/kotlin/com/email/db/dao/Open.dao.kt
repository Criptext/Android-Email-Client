package com.email.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.email.db.models.Open

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
