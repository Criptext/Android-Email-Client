package com.criptext.mail.db.dao

import androidx.room.*
import com.criptext.mail.db.models.Label

/**
 * Created by sebas on 1/24/18.
 */

@Dao
interface LabelDao {

    @Insert
    fun insert(label: Label): Long

    @Insert
    fun insertAll(labels : List<Label>): List<Long>

    @Query("SELECT * FROM label WHERE accountId IS NULL OR accountId = :accountId")
    fun getAll(accountId: Long) : List<Label>

    @Query("SELECT * FROM label WHERE id > :lastId ORDER BY id LIMIT :limit")
    fun getAllForLinkFile(limit: Int, lastId: Long) : List<Label>

    @Query("SELECT * FROM label where type = 'CUSTOM' AND accountId = :accountId")
    fun getAllCustomLabels(accountId: Long) : List<Label>

    @Query("SELECT * FROM label where type = 'CUSTOM' AND visible = 1 AND accountId = :accountId")
    fun getCustomAndVisibleLabels(accountId: Long) : List<Label>

    @Query("SELECT * FROM label where id = :id AND (accountId IS NULL OR accountId = :accountId)")
    fun getLabelById(id: Long, accountId: Long) : Label

    @Delete
    fun deleteMultipleLabels(labels: List<Label>)

    @Delete
    fun delete(label: Label)

    @Query("""SELECT * FROM label
            WHERE text=:labelName AND (accountId IS NULL OR accountId = :accountId)
            LIMIT 1""")
    fun get(labelName: String, accountId: Long): Label

    @Query("""select CAST(COUNT(*) AS BIT) FROM label WHERE text=:labelName""")
    fun alreadyExists(labelName: String): Boolean

    @Query("""SELECT * FROM label
            WHERE text in (:labelNames) AND (accountId IS NULL OR accountId = :accountId)""")
    fun get(labelNames: List<String>, accountId: Long): List<Label>

    @Query("""UPDATE label
            SET visible=:visibility
            where id=:id AND (accountId IS NULL OR accountId = :accountId)""")
    fun updateVisibility(id: Long, visibility: Boolean, accountId: Long)

    @Query("DELETE FROM label WHERE accountId=:accountId")
    fun nukeTable(accountId: Long)

}