package com.criptext.mail.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.criptext.mail.db.ContactTypes
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Email
import com.criptext.mail.db.models.EmailContact

/**
 * Created by sebas on 2/7/18.
 */

@Dao
interface EmailContactJoinDao {


    @Query("""SELECT email.* FROM email
            INNER JOIN email_contact
            ON email.id=email_contact.emailId
            WHERE email_contact.contactId=:contactId""")
    fun getEmailsFromContact(contactId: String) : List<Email>

    @Query("""SELECT contact.* FROM contact
            INNER JOIN email_contact
            ON contact.id=email_contact.contactId
            WHERE email_contact.emailId=:emailId
            AND email_contact.type=:contactType""")
    fun getContactsFromEmail(emailId: Long, contactType: ContactTypes) : List<Contact>

    @Query("""UPDATE contact
            SET score = score + 1
            WHERE id IN
            (SELECT contactId FROM email_contact
            WHERE emailId in (:emailIds) AND type != :excludedType)""")
    fun increaseScore(emailIds: List<Long>, excludedType: ContactTypes)


    @Insert
    fun insertAll(emailUsers : List<EmailContact>)


    @Query("SELECT * FROM email_contact")
    fun getAll() : List<EmailContact>

    @Query("""SELECT * FROM email_contact
        WHERE EXISTS
        (SELECT * FROM email WHERE delivered NOT IN (1, 4)
        AND email.id = email_contact.emailId)
        AND NOT EXISTS
        (SELECT * FROM email_label WHERE email_label.emailId = email_contact.emailId
        AND email_label.labelId=6)
        LIMIT :limit OFFSET :offset""")
    fun getAllForLinkFile(limit: Int, offset: Int) : List<EmailContact>

    @Insert
    fun insert(emailContact : EmailContact)

    @Delete
    fun deleteAll(emailUsers: List<EmailContact>)

    @Query("DELETE FROM email_contact")
    fun nukeTable()

}