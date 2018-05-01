package com.email.scenes.mailbox.data

import com.email.api.models.EmailMetadata
import com.email.db.ContactTypes
import com.email.db.DeliveryTypes
import com.email.db.dao.EmailInsertionDao
import com.email.db.models.*
import com.email.utils.DateUtils
import com.email.utils.HTMLUtils
import kotlin.collections.HashMap

/**
 * Runs the necessary steps before inserting an email like inserting all the contact rows to the
 * database.
 * Created by gabriel on 4/26/18.
 */
object EmailInsertionSetup {
    private fun createEmailRow(metadata: EmailMetadata, decryptedBody: String): Email {
        val bodyWithoutHTML = HTMLUtils.html2text(decryptedBody)
        val preview   = if (bodyWithoutHTML.length > 20 )
                            bodyWithoutHTML.substring(0,20)
                        else bodyWithoutHTML
        return Email(
            id = 0,
            unread = true,
            date = DateUtils.getDateFromString(
                    metadata.date,
                    null),
            threadid = metadata.threadId,
            subject = metadata.subject,
            isTrash = false,
            secure = true,
            preview = preview,
            key = metadata.bodyKey,
            isDraft = false,
            delivered = DeliveryTypes.OPENED,
            content = decryptedBody
        )
    }

    private fun fillMapWithNewContacts(dao: EmailInsertionDao, contactsMap: HashMap<String, Contact>,
                                       toAddresses: List<String>) {
        val unknownContacts = toAddresses.filter { !contactsMap.containsKey(it) }
                .map { Contact(id = 0, email = it, name = "") }

        if (unknownContacts.isNotEmpty()) {
            val insertedContactIds = dao.insertContacts(unknownContacts)

            unknownContacts.forEachIndexed { index, contact ->
                contact.id = insertedContactIds[index]
                contactsMap[contact.email] = contact
            }
        }
    }

    private fun createContactRows(dao: EmailInsertionDao, addressesCSV: String): List<Contact> {
        if (addressesCSV.isEmpty()) return emptyList()

        val toAddresses = addressesCSV.split(",")
        val existingContacts = dao.findContactsByEmail(toAddresses)

        val contactsMap = HashMap<String, Contact>()
        existingContacts.map { Pair(it.email, it) }.toMap(contactsMap)

        fillMapWithNewContacts(dao, contactsMap, toAddresses)

        return contactsMap.values.toList()
    }

    private fun createSenderContactRow(dao: EmailInsertionDao, senderContact: Contact): Contact {
        val existingContacts = dao.findContactsByEmail(listOf(senderContact.email))
        return if (existingContacts.isEmpty()) {
            val ids = dao.insertContacts(listOf(senderContact))
            Contact(id = ids.first(), name = senderContact.name, email = senderContact.email)
        } else existingContacts.first()
    }

    private fun createFullEmailToInsert(dao: EmailInsertionDao, metadata: EmailMetadata, decryptedBody: String,
                                        labels: List<Label>): FullEmail {
        val emailRow = createEmailRow(metadata, decryptedBody)
        val senderContactRow = createSenderContactRow(dao, metadata.fromContact)
        val toContactsRows = createContactRows(dao, metadata.to)
        val ccContactsRows = createContactRows(dao, metadata.cc)
        val bccContactsRows = createContactRows(dao, metadata.bcc)
        return FullEmail(email = emailRow,
                to = toContactsRows,
                cc = ccContactsRows,
                bcc = bccContactsRows,
                labels = labels,
                from = senderContactRow, files = emptyList())
    }

    private fun createEmailContactRelation(newEmailId: Long, type: ContactTypes): (Contact) -> EmailContact =
            { contact -> EmailContact(emailId = newEmailId, contactId = contact.id, type = type) }

    private fun createEmailLabelRelation(newEmailId: Long): (Label) -> EmailLabel =
            { label -> EmailLabel(emailId = newEmailId, labelId = label.id) }

    private fun insertEmailLabelRelations(dao: EmailInsertionDao, fullEmail: FullEmail, newEmailId: Long) {
        val labelRelations = fullEmail.labels.map(createEmailLabelRelation(newEmailId))
        dao.insertEmailLabelRelations(labelRelations)
    }

    private fun insertEmailContactRelations(dao: EmailInsertionDao, fullEmail: FullEmail, newEmailId: Long) {
        val senderRelation = EmailContact(emailId = newEmailId, contactId = fullEmail.from.id,
                type = ContactTypes.FROM)
        val toRelations = fullEmail.to.map(createEmailContactRelation(newEmailId, ContactTypes.TO))
        val ccRelations = fullEmail.cc.map(createEmailContactRelation(newEmailId, ContactTypes.CC))
        val bccRelations = fullEmail.bcc.map(createEmailContactRelation(newEmailId, ContactTypes.BCC))

        val contactRelations = listOf(toRelations, ccRelations, bccRelations)
                .flatten()
                .plus(senderRelation)

        dao.insertEmailContactRelations(contactRelations)
    }


    /**
     * Inserts all the rows and relations needed for a new email
     * @param dao Abstraction for the database methods needed to insert all rows and relations
     * @param metadata metadata of the email to insert
     * @param decryptedBody plain text string with the email's body
     * @param labels list of labels to add to the email once inserted.
     */
    fun exec(dao: EmailInsertionDao, metadata: EmailMetadata, decryptedBody: String, labels: List<Label>) {
        val fullEmail = createFullEmailToInsert(dao, metadata, decryptedBody, labels)
        val newEmailId = dao.insertEmail(fullEmail.email)
        insertEmailLabelRelations(dao, fullEmail, newEmailId)
        insertEmailContactRelations(dao, fullEmail, newEmailId)
    }
}