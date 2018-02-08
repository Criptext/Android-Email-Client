package com.email.DB.DAO

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.email.DB.models.Email

/**
 * Created by sebas on 1/24/18.
 */

@Dao interface EmailDao {

    @Insert
    fun insertAll(emails : List<Email>)

    @Query("SELECT * FROM email")
    fun getAll() : List<Email>

    @Query("SELECT DISTINCT * " +
            "FROM email as e " +
            "WHERE id in (SELECT DISTINCT emailId " +
            "FROM email_label) ")

    fun getNotArchivedEmailThreads() : List<Email>

    @Delete
    fun deleteAll(emails: List<Email>)

    @Query("UPDATE email " +
            "SET unread = :unread " +
            "where id=:id")
    fun toggleRead(id: Int, unread: Boolean)
}
