package com.email.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.email.db.models.Contact
import com.email.db.models.Email
import com.email.db.models.EmailContact

/**
 * Created by sebas on 2/7/18.
 */

@Dao
interface EmailContactJoinDao {

    @Insert
    fun insert(emailContact : EmailContact)

    @Query("""SELECT email.* FROM email
            INNER JOIN email_contact
            ON email.id=email_contact.emailId
            WHERE email_contact.contactId=:contactId""")
    fun getEmailsFromContact(contactId: String) : List<Email>

    @Query("""SELECT contact.* FROM contact
            INNER JOIN email_contact
            ON contact.email=email_contact.contactId
            WHERE email_contact.emailId=:emailId
            AND email_contact.type='CC'""")
    fun getContactsFromEmailCC(emailId: Int) : List<Contact>

    @Query("""SELECT contact.* FROM contact
            INNER JOIN email_contact
            ON contact.email=email_contact.contactId
            WHERE email_contact.emailId=:emailId
            AND email_contact.type='BCC'""")
    fun getContactsFromEmailBCC(emailId: Int) : List<Contact>

    @Query("""SELECT contact.* FROM contact
            INNER JOIN email_contact
            ON contact.email=email_contact.contactId
            WHERE email_contact.emailId=:emailId
            AND email_contact.type='FROM' limit 1""")
    fun getContactsFromEmailFROM(emailId: Int) : Contact?

    @Query("""SELECT contact.* FROM contact
            INNER JOIN email_contact
            ON contact.email=email_contact.contactId
            WHERE email_contact.emailId=:emailId
            AND email_contact.type='TO'""")
    fun getContactsFromEmailTO(emailId: Int) : List<Contact>

    @Insert
    fun insertAll(emailUsers : List<EmailContact>)

    @Query("SELECT * FROM email_contact")
    fun getAll() : List<EmailContact>

    @Delete
    fun deleteAll(emailUsers: List<EmailContact>)

}