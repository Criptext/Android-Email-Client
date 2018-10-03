package com.criptext.mail.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
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


    @Insert
    fun insertAll(emailUsers : List<EmailContact>)


    @Query("SELECT * FROM email_contact")
    fun getAll() : List<EmailContact>

    @Query("""SELECT * FROM email_contact
        WHERE emailId in (:emailIds)""")
    fun getAllForLinkFile(emailIds: List<Long>) : List<EmailContact>

    @Insert
    fun insert(emailContact : EmailContact)

    @Delete
    fun deleteAll(emailUsers: List<EmailContact>)

}