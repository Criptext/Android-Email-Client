package com.email.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Transaction
import com.email.db.ContactTypes
import com.email.db.models.*
import java.util.*

/**
 * Interface that encapsulates all database interactions needed by the mailbox scene.
 * Created by gabriel on 4/23/18.
 */
@Dao
interface MailboxDao {

    @Insert
    fun insertEmails(emails: List<Email>): List<Long>

    @Insert
    fun insertContacts(contacts: List<Contact>): List<Long>

    @Insert
    fun insertEmailLabelRelations(emailLabelRelations: List<EmailLabel>)

    @Insert
    fun insertEmailContactRelations(emailContactRelations: List<EmailContact>)

    private fun createEmailContactRelation(newEmailId: Long, type: ContactTypes): (Long) -> EmailContact =
        { contactId -> EmailContact(emailId = newEmailId, contactId = contactId, type = type) }

    private fun appendEmailRelations(inboxId: Long, newEmailIds: List<Long>)
            : (Int, EmailRelations, EmailContactIds) -> EmailRelations =
            { index, acc, contactIds ->
                val newEmailId = newEmailIds[index]
                acc.emailLabelRelations.add(EmailLabel(emailId = newEmailId, labelId = inboxId))
                acc.emailContactRelations.addAll(contactIds.toIds.map(
                        createEmailContactRelation(newEmailId, ContactTypes.TO)))
                acc.emailContactRelations.addAll(contactIds.ccIds.map(
                        createEmailContactRelation(newEmailId, ContactTypes.CC)))
                acc.emailContactRelations.addAll(contactIds.bccIds.map(
                        createEmailContactRelation(newEmailId, ContactTypes.BCC)))
                acc.emailContactRelations.add(
                        createEmailContactRelation(newEmailId, ContactTypes.FROM)
                                .invoke(contactIds.senderId))
                acc
            }

    @Transaction
    fun insertNewReceivedEmails(newEmails: List<EmailContactIds>) {
        val inboxId = Label.defaultItems.inbox.id

        val newEmailIds = insertEmails(newEmails.map { it.email })

        val rowsToInsert = newEmails.foldIndexed(EmailRelations(),
                appendEmailRelations(inboxId, newEmailIds))

        insertEmailLabelRelations(rowsToInsert.emailLabelRelations)
        insertEmailContactRelations(rowsToInsert.emailContactRelations)
    }

    private class EmailRelations(val emailLabelRelations: MutableList<EmailLabel>,
                                 val emailContactRelations: MutableList<EmailContact>) {
        constructor(): this(LinkedList(), LinkedList())
    }

}
