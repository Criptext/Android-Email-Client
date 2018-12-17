package com.criptext.mail.db.dao

import androidx.room.*
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.models.Email
import java.util.*

/**
 * Created by sebas on 1/24/18.
 */

@Dao interface EmailDao {

    @Insert
    fun insertAll(emails : List<Email>)

    @Query("""SELECT * FROM email
            left join email_label on email.id = email_label.emailId
            WHERE delivered in (:deliveryTypes)
            AND NOT EXISTS
            (SELECT * FROM email_label WHERE email_label.emailId = email.id and email_label.labelId IN (:rejectedLabels))
            """)
    fun getPendingEmails(deliveryTypes: List<Int>, rejectedLabels: List<Long>) : List<Email>

    @Query("SELECT * FROM email")
    fun getAll() : List<Email>

    @Query("""SELECT * FROM email
        WHERE email.id > :lastId
        AND delivered NOT IN (1,4)
        AND NOT EXISTS
        (SELECT * FROM email_label WHERE email_label.emailId = email.id and email_label.labelId=6)
        ORDER BY email.id
        LIMIT :limit
    """)
    fun getAllForLinkFile(limit: Int, lastId: Long) : List<Email>

    @Query("""SELECT * FROM email
                WHERE metadataKey in (:metadataKeys)""")
    fun getAllEmailsByMetadataKey(metadataKeys: List<Long>) : List<Email>

    @Query("""SELECT * FROM email
                WHERE metadataKey in (:metadataKeys)
                AND delivered=3""")
    fun getAllEmailsToOpenByMetadataKey(metadataKeys: List<Long>) : List<Email>

    @Query("""SELECT * FROM email
                WHERE metadataKey in (:metadataKey)""")
    fun getEmailByMetadataKey(metadataKey: Long) : Email

    @Query("""SELECT * FROM email
                WHERE id=:id""")
    fun getEmailById(id: Long) : Email?

    @Query("""SELECT * FROM email
                WHERE id in (:emailIds)""")
    fun getAllEmailsbyId(emailIds: List<Long>) : List<Email>

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
        select email.*,CASE WHEN email.threadId = "" THEN email.id ELSE email.threadId END as uniqueId,
        group_concat(email_label.labelId) as allLabels,
        max(email.unread) as unread, max(email.date)
        from email
        left join email_label on email.id = email_label.emailId
        and date < :startDate
        where case when :isTrashOrSpam
        then email_label.labelId = (select id from label where label.id= cast(trim(:selectedLabel, '%') as integer))
        else not exists
        (select * from email_label where email_label.emailId = email.id and email_label.labelId in (:rejectedLabels))
        end
        group by uniqueId
        having coalesce(allLabels, "") like :selectedLabel
        order by date DESC limit :limit
            """)
    fun getEmailThreadsFromMailboxLabel(
            isTrashOrSpam: Boolean,
            startDate: Date,
            rejectedLabels: List<Long>,
            selectedLabel: String,
            limit: Int ): List<Email>

    @Query("""
        select email.*,CASE WHEN email.threadId = "" THEN email.id ELSE email.threadId END as uniqueId,
        group_concat(email_label.labelId) as allLabels,
        max(email.unread) as unread, max(email.date)
        from email
        left join email_label on email.id = email_label.emailId
        and date > :startDate
        where case when :isTrashOrSpam
        then email_label.labelId = (select id from label where label.id= cast(trim(:selectedLabel, '%') as integer))
        else not exists
        (select * from email_label where email_label.emailId = email.id and email_label.labelId in (:rejectedLabels))
        end
        group by uniqueId
        having coalesce(allLabels, "") like :selectedLabel
        order by date DESC
            """)
    fun getNewEmailThreadsFromMailboxLabel(
            isTrashOrSpam: Boolean,
            startDate: Date,
            rejectedLabels: List<Long>,
            selectedLabel: String): List<Email>

    @Query("""
        select email.*,CASE WHEN email.threadId = "" THEN email.id ELSE email.threadId END as uniqueId,
        max(email.unread) as unread, max(email.date),
        group_concat(distinct(contact.name)) as contactNames,
        group_concat(distinct(contact.email)) as contactEmails
        from email
        inner join email_label on email.id = email_label.emailId
        left join email_contact on email.id = email_contact.emailId
        left join contact on email_contact.contactId = contact.id
        and date < :starterDate
        where not exists
        (select * from email_label where email_label.emailId = email.id and email_label.labelId in (:rejectedLabels))
        group by uniqueId
        having contactNames like :queryText
        or contactEmails like :queryText
        or bodyPreview like :queryText
        or content like :queryText
        or subject like :queryText
        order by date DESC limit :limit
        """)
    fun searchEmailThreads(
            starterDate: Date,
            queryText: String,
            rejectedLabels: List<Long>,
            limit: Int): List<Email>

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
            SET unread=:unread
            where metadataKey in (:metadataKeys)""")
    fun toggleReadByMetadataKey(metadataKeys: List<Long>, unread: Boolean)

    @Query("""UPDATE email
            SET unread=:unread
            where id in (:ids) AND unread !=:unread""")
    fun toggleCheckingRead(ids: List<Long>, unread: Boolean)

    @Query("""UPDATE email
            SET unread=:unread
            where threadId in (:threadIds)""")
    fun toggleReadByThreadId(threadIds: List<String>, unread: Boolean)

    @Query("""SELECT threadId FROM email
            where id in (:emailIds)""")
    fun getThreadIdsFromEmailIds(emailIds: List<Long>): List<String>

    @Query("""SELECT DISTINCT threadId FROM email
            left join email_label on email.id = email_label.emailId
            where email_label.labelId=:labelId""")
    fun getThreadIdsFromLabel(labelId: Long): List<String>

    @Query("""SELECT DISTINCT metadataKey FROM email
            left join email_label on email.id = email_label.emailId
            where email_label.labelId=:labelId""")
    fun getMetadataKeysFromLabel(labelId: Long): List<Long>

    @Query("""SELECT DISTINCT emailId FROM email
            left join email_label on email.id = email_label.emailId
            where email_label.labelId=:labelId
            AND (julianday('now') - julianday(email.trashDate)) >= 30""")
    fun getTrashExpiredThreadIds(labelId: Long): List<Long>

    @Query("""UPDATE email
            SET trashDate=:trashDate
            where id in (:emailIds)""")
    fun updateEmailTrashDate(trashDate: Date, emailIds: List<Long>)

    @Query("""UPDATE email
            SET threadId=:threadId,
            messageId=:messageId,
            metadataKey=:metadataKey,
            date=:date,
            delivered=:status
            where id=:id""")
    fun updateEmail(id: Long, threadId: String, messageId: String, metadataKey: Long, date: Date, status: DeliveryTypes)

    @Update
    fun update(emails: List<Email>)

    @Query("""SELECT * FROM email
            left join email_label on email.id = email_label.emailId
            WHERE threadId=:threadId
            AND NOT EXISTS
            (SELECT * FROM email_label WHERE email_label.emailId = email.id and email_label.labelId IN (:rejectedLabels))
            GROUP BY email.messageId,email.threadId
            ORDER BY date ASC""")
    fun getEmailsFromThreadId(threadId: String, rejectedLabels: List<Long>): List<Email>

    @Query("""SELECT * FROM email
            left join email_label on email.id = email_label.emailId
            WHERE threadId=:threadId
            AND email_label.labelId = :labelId
            GROUP BY email.messageId,email.threadId
            ORDER BY date ASC""")
    fun getEmailsFromThreadIdByLabel(labelId: Long, threadId: List<String>): List<Email>

    @Query("""SELECT * FROM email
            WHERE threadId in (:threadIds)""")
    fun getEmailsFromThreadIds(threadIds: List<String>): List<Email>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(email: Email): Long

    @Query("""UPDATE email
            SET delivered=:deliveryType
            WHERE id=:id""")
    fun changeDeliveryType(id: Long, deliveryType: DeliveryTypes)

    @Query("""UPDATE email
            SET delivered=:deliveryType
            WHERE metadataKey=:metadataKey""")
    fun changeDeliveryTypeByMetadataKey(metadataKey: Long, deliveryType: DeliveryTypes)

    @Query("""UPDATE email
            SET content=:content,
            bodyPreview=:preview,
            unsentDate=:unsentDate
            WHERE id=:id""")
    fun unsendEmailById(id: Long, content: String, preview: String, unsentDate: Date)

    @Query("""UPDATE email
            SET content=:content,
            bodyPreview=:preview,
            unsentDate=:unsentDate
            WHERE metadataKey=:metadataKey""")
    fun unsendEmailByMetadataKey(metadataKey: Long, content: String, preview: String, unsentDate: Date)

    @Query("""UPDATE email
            SET content=:content,
            bodyPreview=:preview,
            unsentDate=:unsentDate
            WHERE metadataKey in (:metadataKeys)""")
    fun unsendEmailsByMetadataKey(metadataKeys: List<Long>, content: String, preview: String, unsentDate: Date)


    @Query("""UPDATE email
            SET delivered=:deliveryType
            WHERE id in (:ids)""")
    fun changeDeliveryType(ids: List<Long>, deliveryType: DeliveryTypes)

    @Query("""UPDATE email
            SET delivered=:deliveryType
            WHERE metadataKey in (:keys) AND delivered not in (:nonChangeableTypes)""")
    fun changeDeliveryTypeByMetadataKey(keys: List<Long>, deliveryType: DeliveryTypes,
                                        nonChangeableTypes: List<Int>)

    @Query("""SELECT *
            FROM email
            WHERE metadataKey=:key""")
    fun findEmailByMetadataKey(key: Long): Email?

    @Query("""SELECT *
            FROM email
            WHERE id=:id""")
    fun findEmailById(id: Long): Email?

    @Query("""
        select email.*,CASE WHEN email.threadId = "" THEN email.id ELSE email.threadId END as uniqueId,
        group_concat(email_label.labelId) as allLabels,
        max(email.unread) as unread, max(email.date)
        from email
        left join email_label on email.id = email_label.emailId
        where case when :isTrashOrSpam
        then email_label.labelId = (select id from label where label.id= cast(trim(:selectedLabel, '%') as integer))
        else not exists
        (select * from email_label where email_label.emailId = email.id and email_label.labelId in (:rejectedLabels))
        end
        group by uniqueId
        having coalesce(allLabels, "") like :selectedLabel
        order by date DESC limit :limit
        """)
    fun getInitialEmailThreadsFromMailboxLabel(
            isTrashOrSpam: Boolean,
            rejectedLabels: List<Long>,
            selectedLabel: String,
            limit: Int): List<Email>

    @Query("""
        select email.*,CASE WHEN email.threadId = "" THEN email.id ELSE email.threadId END as uniqueId,
        max(email.unread) as unread, max(email.date),
        group_concat(distinct(contact.name)) as contactNames,
        group_concat(distinct(contact.email)) as contactEmails
        from email
        inner join email_label on email.id = email_label.emailId
        left join email_contact on email.id = email_contact.emailId
        left join contact on email_contact.contactId = contact.id
        where not exists
        (select * from email_label where email_label.emailId = email.id and email_label.labelId in (:rejectedLabels))
        group by uniqueId
        having contactNames like :queryText
        or contactEmails like :queryText
        or bodyPreview like :queryText
        or content like :queryText
        or subject like :queryText
        order by date DESC limit :limit
        """)
    fun searchInitialEmailThreads(
            queryText: String,
            rejectedLabels: List<Long>,
            limit: Int): List<Email>

    @Query("""
        select email.*, CASE WHEN email.threadId = "" THEN email.id ELSE email.threadId END as uniqueId,
        group_concat(email_label.labelId) as allLabels,
        max(email.unread) as unread
        from email
        left join email_label on email.id = email_label.emailId
        where not exists
        (select * from email_label where email_label.emailId = email.id and email_label.labelId in (:rejectedLabels))
        and unread = 1
        group by uniqueId
        having coalesce(allLabels, "") like :selectedLabel
        """)
    fun getTotalUnreadThreads(rejectedLabels: List<Long>, selectedLabel: String): List<Email>

    @Query("""
        select email.*, CASE WHEN email.threadId = "" THEN email.id ELSE email.threadId END as uniqueId,
        group_concat(email_label.labelId) as allLabels,
        max(email.unread) as unread
        from email
        left join email_label on email.id = email_label.emailId
        group by uniqueId
        having coalesce(allLabels, "") like :selectedLabel
        """)
    fun getTotalThreads(selectedLabel: String): List<Email>

    @Query("""
        select count(distinct(email.id)) from email
        left join email_label on email.id = email_label.emailId
        where threadId=:threadId
        and not exists
        (select * from email_label where email_label.emailId = email.id and email_label.labelId in (:rejectedLabels))
        """)
    fun getTotalEmailsByThread(threadId: String, rejectedLabels: List<Long>): Int

    @Query("DELETE from email WHERE id = :id")
    fun deleteById(id: Long)

    @Query("DELETE from email WHERE metadataKey in (:metadataKeys)")
    fun deleteByIds(metadataKeys: List<Long>)

    @Query("DELETE from email WHERE threadId in (:threadIds)")
    fun deleteThreads(threadIds: List<String>)

    @Query("""DELETE from email WHERE threadId in (:threadIds)
        AND id IN
        (SELECT email.id FROM email LEFT JOIN email_label ON email.id = email_label.emailId
            WHERE email_label.labelId in (:labels))
    """)
    fun deleteThreads(threadIds: List<String>, labels: List<Long>)

    @Query("DELETE FROM email")
    fun nukeTable()

}
