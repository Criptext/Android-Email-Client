package com.email.db.dao

import android.arch.persistence.room.*
import com.email.db.LabelTextTypes
import com.email.db.models.Label

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
    fun deleteMultipleLabels(labels: List<Label>)

    @Delete
    fun delete(label: Label)


    @Query("""SELECT * FROM label
            WHERE text=:labelTextType
            LIMIT 1""")
    fun get(labelTextType: LabelTextTypes): Label

}