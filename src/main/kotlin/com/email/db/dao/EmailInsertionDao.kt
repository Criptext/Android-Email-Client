package com.email.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import com.email.db.ContactTypes
import com.email.db.models.*
import java.util.*

/**
 * Interface that encapsulates all database interactions needed by the mailbox scene.
 * Created by gabriel on 4/23/18.
 */
@Dao
interface EmailInsertionDao {

    @Insert
    fun insertEmail(email: Email): Long

    @Query("Select * from email where key = :bodyKey")
    fun findEmailByBodyKey(bodyKey: String): Email

    @Insert
    fun insertEmails(emails: List<Email>): List<Long>

    @Insert
    fun insertContacts(contacts: List<Contact>): List<Long>

    @Query("SELECT * FROM contact where email in (:emailAddresses)")
    fun findContactsByEmail(emailAddresses: List<String>): List<Contact>

    @Insert
    fun insertEmailLabelRelations(emailLabelRelations: List<EmailLabel>)

    @Insert
    fun insertEmailContactRelations(emailContactRelations: List<EmailContact>)

    private fun createEmailContactRelation(newEmailId: Long, type: ContactTypes): (Long) -> EmailContact =
        { contactId -> EmailContact(emailId = newEmailId, contactId = contactId, type = type) }

    @Transaction
    fun runTransaction(runnable: Runnable) {
        runnable.run()
    }
}
