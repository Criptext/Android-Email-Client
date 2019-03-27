package com.criptext.mail.db.dao

import androidx.room.*
import com.criptext.mail.db.models.*

/**
 * Interface that encapsulates all database interactions needed by the mailbox scene.
 * Created by gabriel on 4/23/18.
 */
@Dao
interface EmailInsertionDao {

    @Insert
    fun insertEmail(email: Email): Long

    @Query("SELECT * FROM email")
    fun getAll(): List<Email>

    @Query("DELETE FROM email WHERE id = :draftEmailId AND accountId = :accountId" )
    fun deletePreviouslyCreatedDraft(draftEmailId: Long, accountId: Long)

    @Query("Select * from email where messageId = :messageId AND accountId = :accountId")
    fun findEmailByMessageId(messageId: String, accountId: Long): Email?

    @Query("Select * from email where metadataKey = :metadataKey AND accountId = :accountId")
    fun findEmailByMetadataKey(metadataKey: Long, accountId: Long): Email?

    @Query("Select * from email where id=:id AND accountId = :accountId")
    fun findEmailById(id: Long, accountId: Long): Email?

    @Insert
    fun insertEmails(emails: List<Email>): List<Long>

    @Insert
    fun insertContacts(contacts: List<Contact>): List<Long>

    @Query("""UPDATE contact
            SET name=:name
            where id=:id
            AND EXISTS
            (SELECT DISTINCT * FROM contact LEFT JOIN account_contact ON contact.id = account_contact.contactId WHERE account_contact.accountId=:accountId)""")
    fun updateContactName(id: Long, name: String, accountId: Long)

    @Query("SELECT * FROM contact where email in (:emailAddresses)")
    fun findContactsByEmail(emailAddresses: List<String>): List<Contact>

    @Query("SELECT * FROM file where emailId=:id")
    fun findFilesByEmailId(id: Long): List<CRFile>

    @Insert
    fun insertEmailLabelRelations(emailLabelRelations: List<EmailLabel>)

    @Query("""DELETE FROM email_label
        WHERE labelId= :labelId AND emailId=:emailId""")
    fun deleteByEmailLabelIds(labelId: Long, emailId: Long)

    @Insert
    fun insertEmailContactRelations(emailContactRelations: List<EmailContact>)

    @Insert
    fun insertEmailFiles(emailFiles: List<CRFile>)

    @Insert
    fun insertEmailFileKey(emailFileKey: FileKey)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAccountContact(accountContact: List<AccountContact>)

    @Transaction
    fun runTransaction(insertFn: () -> Long): Long {
        return insertFn()
    }
}
