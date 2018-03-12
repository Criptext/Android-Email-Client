package com.email.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.email.db.models.Email
import com.email.db.models.EmailContact

/**
 * Created by sebas on 2/7/18.
 */

@Dao
interface EmailContactJoinDao {

    @Insert
    fun insert(emailContact : EmailContact)

    @Query("SELECT latestEmail.* FROM latestEmail " +
            "INNER JOIN email_contact " +
            "ON latestEmail.id=email_contact.emailId " +
            "WHERE email_contact.contactId=:contactId")
    fun getEmailsFromContact(contactId: String) : List<Email>

    @Insert
    fun insertAll(emailUsers : List<EmailContact>)

    @Query("SELECT * FROM email_contact")
    fun getAll() : List<EmailContact>

    @Delete
    fun deleteAll(emailUsers: List<EmailContact>)

}