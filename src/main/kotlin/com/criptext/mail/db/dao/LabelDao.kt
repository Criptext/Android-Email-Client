package com.criptext.mail.db.dao

import android.arch.persistence.room.*
import com.criptext.mail.db.models.Label

/**
 * Created by sebas on 1/24/18.
 */

@Dao
interface LabelDao {

    @Insert
    fun insert(label: Label): Long

    @Insert
    fun insertAll(labels : List<Label>)

    @Query("SELECT * FROM label")
    fun getAll() : List<Label>

    @Query("SELECT * FROM label WHERE id > :lastId ORDER BY id LIMIT :limit")
    fun getAllForLinkFile(limit: Int, lastId: Long) : List<Label>

    @Query("SELECT * FROM label where type = 'CUSTOM'")
    fun getAllCustomLabels() : List<Label>

    @Query("SELECT * FROM label where type = 'CUSTOM' AND visible = 1")
    fun getCustomAndVisibleLabels() : List<Label>

    @Query("SELECT * FROM label where id = :id")
    fun getLabelById(id: Long) : Label

    @Delete
    fun deleteMultipleLabels(labels: List<Label>)

    @Delete
    fun delete(label: Label)

    @Query("""SELECT * FROM label
            WHERE text=:labelName
            LIMIT 1""")
    fun get(labelName: String): Label

    @Query("""select CAST(COUNT(*) AS BIT) FROM label WHERE text=:labelName""")
    fun alreadyExists(labelName: String): Boolean

    @Query("""SELECT * FROM label
            WHERE text in (:labelNames)""")
    fun get(labelNames: List<String>): List<Label>

    @Query("""UPDATE label
            SET visible=:visibility
            where id=:id""")
    fun updateVisibility(id: Long, visibility: Boolean)

    @Query("DELETE FROM label")
    fun nukeTable()

}