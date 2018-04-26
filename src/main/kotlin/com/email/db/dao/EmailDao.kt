package com.email.db.dao

import android.arch.persistence.room.*
import com.email.db.DeliveryTypes
import com.email.db.models.Email
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
        DESC LIMIT :limit
            """)
    fun getEmailThreadsFromMailboxLabel(
            starterDate: Date,
            rejectedLabels: List<Long>,
            selectedLabel: Long,
            limit: Int ): List<Email>

    @Query("""
        SELECT * FROM email e
        WHERE threadid=:threadId AND date=(SELECT MAX(date)
        FROM email d WHERE d.threadid=:threadId)
        GROUP BY threadid
        LIMIT 1
            """)
    fun getLatestEmailFromThreadId(
            threadId: String): Email

    @Delete
    fun deleteAll(emails: List<Email>)

    @Query("""UPDATE email
            SET unread=:unread
            where id=:id""")
    fun toggleRead(id: Long, unread: Boolean)

    @Query("""UPDATE email
            SET threadid=:threadId,
            key=:key,
            date=:date,
            delivered=:status
            where id=:id""")
    fun updateEmail(id: Long, threadId: String, key : String, date: Date, status: DeliveryTypes)

    @Update
    fun update(emails: List<Email>)

    @Query("""SELECT * FROM email
            WHERE threadid=:threadId
            ORDER BY date ASC""")
    fun getEmailsFromThreadId(threadId: String): List<Email>

    @Insert
    fun insert(email: Email): Long

    @Query("""UPDATE email
            SET delivered=:deliveryType
            where id=:id""")
    fun changeDeliveryType(id: Long, deliveryType: DeliveryTypes)

    @Query("""
        SELECT * FROM email e
        WHERE date=(SELECT MAX(date)
        FROM email d WHERE d.threadid=e.threadid)
        AND id IN (SELECT DISTINCT emailId FROM email_label WHERE labelId=:selectedLabel)
        AND id NOT IN (SELECT DISTINCT emailId FROM email_label WHERE labelId in (:rejectedLabels))
        GROUP BY threadid
        ORDER BY date
        DESC LIMIT :limit
            """)
    fun getInitialEmailThreadsFromMailboxLabel(
            rejectedLabels: List<Long>,
            selectedLabel: Long,
            limit: Int): List<Email>

    @Query("""
        SELECT * from email e
        WHERE date=(SELECT MAX(date)
        FROM email d WHERE d.threadid=e.threadid)
        AND id IN (SELECT DISTINCT emailId FROM email_label WHERE labelId=:selectedLabel)
        AND id NOT IN (SELECT DISTINCT emailId FROM email_label WHERE labelId in (:rejectedLabels))
        AND unread = 1
        GROUP BY threadid
        """)
    fun getTotalUnreadThreads(rejectedLabels: List<Int>, selectedLabel: Long): List<Email>

    @Query("""
        SELECT * from email e
        WHERE date=(SELECT MAX(date)
        FROM email d WHERE d.threadid=e.threadid)
        AND id IN (SELECT DISTINCT emailId FROM email_label WHERE labelId=:selectedLabel)
        GROUP BY threadid
        """)
    fun getTotalThreads(selectedLabel: Long): List<Email>
}
