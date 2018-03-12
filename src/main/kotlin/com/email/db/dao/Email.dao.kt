package com.email.db.dao

import android.arch.persistence.room.*
import com.email.db.models.Email

/**
 * Created by sebas on 1/24/18.
 */

@Dao interface EmailDao {

    @Insert
    fun insertAll(emails : List<Email>)

    @Query("SELECT * FROM email e " +
            "WHERE date=(SELECT MAX(date) FROM email " +
            "WHERE id = e.id)")
    fun getAll() : List<Email>

    @Query("SELECT * FROM email e " +
            "WHERE date=(SELECT MAX(date) FROM email " +
            "WHERE id = e.id) AND id in (SELECT DISTINCT emailId " +
            "FROM email_label)")
    fun getNotArchivedEmailThreads() : List<Email>

    @Delete
    fun deleteAll(emails: List<Email>)

    @Query("UPDATE email " +
            "SET unread = :unread " +
            "where id=:id")
    fun toggleRead(id: Int, unread: Boolean)

    @Update
    fun update(emails: List<Email>)
}
