package com.email.db.dao

import android.arch.persistence.room.*
import com.email.db.DeliveryTypes
import com.email.db.LabelTextTypes
import com.email.db.models.Email
import com.email.db.models.Label
import java.util.*

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

    @Query("""
            SELECT * FROM email e
            WHERE date=(SELECT MAX(date) FROM email d
            WHERE d.threadid=e.threadid) AND id
            IN (SELECT DISTINCT emailId
            FROM email_label) GROUP BY threadid
            ORDER BY date DESC
            """)
    fun getNotArchivedEmailThreads() : List<Email>

    @Query("""
        SELECT * FROM email e
        WHERE date=(SELECT MAX(date)
        FROM email d WHERE d.threadid=e.threadid)
        AND date<:starterDate
        AND id IN (SELECT DISTINCT emailId FROM email_label WHERE labelId=:selectedLabel)
        AND id NOT IN (SELECT DISTINCT emailId FROM email_label WHERE labelId in (:rejectedLabels))
        GROUP BY threadid
        ORDER BY date
        DESC LIMIT :offset
            """)
    fun getEmailThreadsFromMailboxLabel(
            starterDate: Date,
            rejectedLabels: List<Int>,
            selectedLabel: Int,
            offset: Int ): List<Email>

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

    @Query("""UPDATE email
            SET delivered=:deliveryType
            where id=:id""")
    fun changeDeliveryType(id: Int, deliveryType: DeliveryTypes)

    @Query("""
        SELECT * FROM email e
        WHERE date=(SELECT MAX(date)
        FROM email d WHERE d.threadid=e.threadid)
        AND id IN (SELECT DISTINCT emailId FROM email_label WHERE labelId=:selectedLabel)
        AND id NOT IN (SELECT DISTINCT emailId FROM email_label WHERE labelId in (:rejectedLabels))
        GROUP BY threadid
        ORDER BY date
        DESC LIMIT :offset
            """)
    fun getInitialEmailThreadsFromMailboxLabel(
            rejectedLabels: List<Int>,
            selectedLabel: Int,
            offset: Int): List<Email>
}
