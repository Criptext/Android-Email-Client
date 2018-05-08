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
            WHERE d.threadId=e.threadId) GROUP BY threadId
            ORDER BY date DESC
            """)
    fun getLatestEmails() : List<Email>

    @Query("""
            SELECT * FROM email e
            WHERE date=(SELECT MAX(date) FROM email d
            WHERE d.threadId=e.threadId) AND id
            IN (SELECT DISTINCT emailId
            FROM email_label) GROUP BY threadId
            ORDER BY date DESC
            """)
    fun getNotArchivedEmailThreads() : List<Email>

    @Query("""
        select email.*, CASE WHEN email.threadId = "" THEN email.id ELSE email.threadId END as uniqueId,
        group_concat(email_label.labelId) as allLabels,
        max(email.unread) as unread
        from email
        inner join email_label on email.id = email_label.emailId
        WHERE email_label.labelId NOT IN (:rejectedLabels)
        AND date<:starterDate
        group by uniqueId
        having allLabels like :selectedLabel
        order by date DESC limit :limit
            """)
    fun getEmailThreadsFromMailboxLabel(
            starterDate: Date,
            rejectedLabels: List<Long>,
            selectedLabel: Long,
            limit: Int ): List<Email>

    @Query("""
        SELECT * FROM email e
        WHERE threadId=:threadId AND date=(SELECT MAX(date)
        FROM email d WHERE d.threadId=:threadId)
        GROUP BY threadId
        LIMIT 1
            """)
    fun getLatestEmailFromThreadId(
            threadId: String): Email

    @Delete
    fun deleteAll(emails: List<Email>)

    @Query("""UPDATE email
            SET unread=:unread
            where id in (:ids)""")
    fun toggleRead(ids: List<Long>, unread: Boolean)

    @Query("""UPDATE email
            SET threadId=:threadId,
            messageId=:messageId,
            date=:date,
            delivered=:status
            where id=:id""")
    fun updateEmail(id: Long, threadId: String, messageId: String, date: Date, status: DeliveryTypes)

    @Update
    fun update(emails: List<Email>)

    @Query("""SELECT * FROM email
            WHERE threadId=:threadId
            ORDER BY date ASC""")
    fun getEmailsFromThreadId(threadId: String): List<Email>

    @Insert
    fun insert(email: Email): Long

    @Query("""UPDATE email
            SET delivered=:deliveryType
            where id=:id""")
    fun changeDeliveryType(id: Long, deliveryType: DeliveryTypes)

    @Query("""
        select email.*, CASE WHEN email.threadId = "" THEN email.id ELSE email.threadId END as uniqueId,
        group_concat(email_label.labelId) as allLabels,
        max(email.unread) as unread
        from email
        inner join email_label on email.id = email_label.emailId
        WHERE email_label.labelId NOT IN (:rejectedLabels)
        group by uniqueId
        having allLabels like :selectedLabel
        order by date DESC limit :limit
        """)
    fun getInitialEmailThreadsFromMailboxLabel(
            rejectedLabels: List<Long>,
            selectedLabel: String,
            limit: Int): List<Email>

    @Query("""
        select email.*, CASE WHEN email.threadId = "" THEN email.id ELSE email.threadId END as uniqueId,
        group_concat(email_label.labelId) as allLabels,
        max(email.unread) as unread
        from email
        inner join email_label on email.id = email_label.emailId
        WHERE email_label.labelId NOT IN (:rejectedLabels)
        and unread = 1
        group by uniqueId
        having allLabels like :selectedLabel
        """)
    fun getTotalUnreadThreads(rejectedLabels: List<Int>, selectedLabel: Long): List<Email>

    @Query("""
        select email.*, CASE WHEN email.threadId = "" THEN email.id ELSE email.threadId END as uniqueId,
        group_concat(email_label.labelId) as allLabels,
        max(email.unread) as unread
        from email
        inner join email_label on email.id = email_label.emailId
        group by uniqueId
        having allLabels like :selectedLabel
        """)
    fun getTotalThreads(selectedLabel: Long): List<Email>

    @Query("""
        select count(*) from email
        where threadId=:threadId
        """)
    fun getTotalEmailsByThread(threadId: String): Int
}
