package com.email.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.email.db.ContactTypes
import com.email.db.models.Contact
import com.email.db.models.Email
import com.email.db.models.EmailContact

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

    @Insert
    fun insert(emailContact : EmailContact)

    @Delete
    fun deleteAll(emailUsers: List<EmailContact>)

}