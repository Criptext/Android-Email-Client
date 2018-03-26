package com.email.db.dao

import android.arch.persistence.room.*
import com.email.db.models.Email

/**
 * Created by sebas on 1/24/18.
 */

@Dao interface EmailDao {

    @Insert
    fun insertAll(emails : List<Email>)

    @Query("SELECT * FROM email")
    fun getAll() : List<Email>

    @Query("""SELECT * from email e
            WHERE date=(SELECT MAX(date) FROM email d
            WHERE d.threadid=e.threadid) GROUP BY threadid
            ORDER BY date DESC
            """)
    fun getLatestEmails() : List<Email>

    @Query("""SELECT * FROM email e
            WHERE date=(SELECT MAX(date) FROM email d
            WHERE d.threadid=e.threadid) AND id
            IN (SELECT DISTINCT emailId
            FROM email_label) GROUP BY threadid
            ORDER BY date DESC
            """)
    fun getNotArchivedEmailThreads() : List<Email>

    @Delete
    fun deleteAll(emails: List<Email>)

    @Query("""UPDATE email
            SET unread=:unread
            where id=:id""")
    fun toggleRead(id: Int, unread: Boolean)

    @Update
    fun update(emails: List<Email>)

    @Query("""SELECT * FROM email
            WHERE threadid=:threadId
            ORDER BY date ASC""")
    fun getEmailsFromThreadId(threadId: String): List<Email>

    @Insert
    fun insert(email: Email)


    @Query("""SELECT MAX(id) FROM email""")
    fun getLastInsertedId(): Int
}
