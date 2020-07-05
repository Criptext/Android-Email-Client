package com.criptext.mail.db.dao

import androidx.room.*
import com.criptext.mail.db.models.Contact

/**
 * Created by sebas on 2/7/18.
 */

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertIgnoringConflicts(contact : Contact): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllIgnoringConflicts(contact : List<Contact>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(users : List<Contact>): List<Long>

    @Query("""SELECT DISTINCT * FROM contact""")
    fun getAll() : List<Contact>

    @Query("""SELECT DISTINCT * FROM contact
        WHERE email in (:emails)
    """)
    fun getContactByEmails(emails: List<String>) : List<Contact>

    @Query("""SELECT DISTINCT * FROM contact
            WHERE id > :lastId
            AND EXISTS
            (SELECT DISTINCT contactId FROM account_contact WHERE accountId=:accountId)
            ORDER BY id LIMIT :limit""")
    fun getAllForLinkFile(limit: Int, lastId: Long, accountId: Long) : List<Contact>

    @Query("""SELECT DISTINCT * FROM contact WHERE email=:email
        AND EXISTS
        (SELECT DISTINCT contactId FROM account_contact WHERE accountId=:accountId)
    """)
    fun getContact(email : String, accountId: Long) : Contact?

    @Query("SELECT * FROM contact where id=:id")
    fun getContactById(id : Long) : Contact?

    @Delete
    fun deleteAll(contacts: List<Contact>)

    @Query("""UPDATE contact
            SET name=:name
            where email=:email
            """)
    fun updateContactName(email: String, name: String)

    @Query("""UPDATE contact
            SET name=:name
            where email=:email
            AND EXISTS
            (SELECT DISTINCT * FROM contact LEFT JOIN account_contact ON contact.id = account_contact.contactId WHERE account_contact.accountId=:accountId)""")
    fun updateContactName(email: String, name: String, accountId: Long)

    @Query("""UPDATE contact
            SET isTrusted=:isTrusted
            where email=:email""")
    fun updateContactIsTrusted(email: String, isTrusted: Boolean)


    @Query("""UPDATE contact
            SET isTrusted=:isTrusted
            where email in (:emails)""")
    fun updateContactsIsTrusted(emails: List<String>, isTrusted: Boolean)

    @Query("""UPDATE contact
            SET spamScore = (spamScore + 1)
            where email in (:emails)
            AND EXISTS
            (SELECT DISTINCT * FROM contact LEFT JOIN account_contact ON contact.id = account_contact.contactId WHERE account_contact.accountId=:accountId)""")
    fun uptickSpamCounter(emails: List<String>, accountId: Long)

    @Query("""UPDATE contact
            SET spamScore = 0
            where email in (:emails)
            AND EXISTS
            (SELECT DISTINCT * FROM contact LEFT JOIN account_contact ON contact.id = account_contact.contactId WHERE account_contact.accountId=:accountId)""")
    fun resetSpamCounter(emails: List<String>, accountId: Long)

    @Query("DELETE FROM contact WHERE  EXISTS (SELECT DISTINCT * FROM contact LEFT JOIN account_contact ON contact.id = account_contact.contactId WHERE account_contact.accountId=:accountId)")
    fun nukeTable(accountId: Long)
}
