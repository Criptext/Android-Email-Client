package com.email.DB.DAO

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.email.DB.models.Label

/**
 * Created by sebas on 1/24/18.
 */

@Dao
interface LabelDao {
    @Insert
    fun insertAll(labels : List<Label>)

    @Query("SELECT * FROM label")
    fun getAll() : List<Label>

    @Delete
    fun deleteAll(labels: List<Label>)
}