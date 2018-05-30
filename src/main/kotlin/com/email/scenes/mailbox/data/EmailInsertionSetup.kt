package com.email.scenes.mailbox.data

import com.email.api.EmailInsertionAPIClient
import com.email.api.models.EmailMetadata
import com.email.db.ContactTypes
import com.email.db.DeliveryTypes
import com.email.db.dao.EmailInsertionDao
import com.email.db.models.*
import com.email.signal.SignalClient
import com.email.signal.SignalEncryptedData
import com.email.utils.DateUtils
import com.email.utils.HTMLUtils
import org.whispersystems.libsignal.DuplicateMessageException
import kotlin.collections.HashMap

/**
 * Runs the necessary steps before inserting an email like inserting all the contact rows to the
 * database.
 * Created by gabriel on 4/26/18.
 */
object EmailInsertionSetup {
    private fun createEmailRow(metadata: EmailMetadata, decryptedBody: String): Email {
        val preview = HTMLUtils.createEmailPreview(decryptedBody)
        return Email(
            id = 0,
            unread = true,
            date = DateUtils.getDateFromString(
                    metadata.date,
                    null),
            threadId = metadata.threadId,
            subject = metadata.subject,
            secure = true,
            preview = preview,
            messageId = metadata.messageId,
            delivered = DeliveryTypes.NONE,
            content = decryptedBody
        )
    }

    private fun fillMapWithNewContacts(dao: EmailInsertionDao, contactsMap: HashMap<String, Contact>,
                                       toAddresses: List<String>) {
        val unknownContacts = toAddresses.filter { !contactsMap.containsKey(it) }
                .map { Contact(id = 0, email = it, name = it.split("@")[0]) }

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
        val toAddressesNotDuplicated = toAddresses.map { Pair(it, it) }.toMap().values.toList()
        val existingContacts = dao.findContactsByEmail(toAddressesNotDuplicated)

        val contactsMap = HashMap<String, Contact>()
        existingContacts.map { Pair(it.email, it) }.toMap(contactsMap)

        fillMapWithNewContacts(dao, contactsMap, toAddressesNotDuplicated)

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
            { contact -> EmailContact(id = 0, emailId = newEmailId, contactId = contact.id, type = type) }

    private fun createEmailLabelRelation(newEmailId: Long): (Label) -> EmailLabel =
            { label -> EmailLabel(emailId = newEmailId, labelId = label.id) }

    private fun insertEmailLabelRelations(dao: EmailInsertionDao, fullEmail: FullEmail, newEmailId: Long) {
        val labelRelations = fullEmail.labels.map(createEmailLabelRelation(newEmailId))
        dao.insertEmailLabelRelations(labelRelations)
    }

    private fun insertEmailContactRelations(dao: EmailInsertionDao, fullEmail: FullEmail, newEmailId: Long) {
        val senderRelation = EmailContact(id = 0, emailId = newEmailId,
                contactId = fullEmail.from.id, type = ContactTypes.FROM)
        val toRelations = fullEmail.to.map(createEmailContactRelation(newEmailId, ContactTypes.TO))
        val ccRelations = fullEmail.cc.map(createEmailContactRelation(newEmailId, ContactTypes.CC))
        val bccRelations = fullEmail.bcc.map(createEmailContactRelation(newEmailId, ContactTypes.BCC))

        val contactRelations = listOf(toRelations, ccRelations, bccRelations)
                .flatten()
                .plus(senderRelation)

        dao.insertEmailContactRelations(contactRelations)
    }


    fun exec(dao: EmailInsertionDao, metadata: EmailMetadata, decryptedBody: String,
                     labels: List<Label>) {
        val fullEmail = createFullEmailToInsert(dao, metadata, decryptedBody, labels)
        exec(dao, fullEmail)
    }

    fun exec(dao: EmailInsertionDao, fullEmail: FullEmail) {
        val newEmailId = dao.insertEmail(fullEmail.email)
        insertEmailLabelRelations(dao, fullEmail, newEmailId)
        insertEmailContactRelations(dao, fullEmail, newEmailId)
    }

    private fun decryptMessage(signalClient: SignalClient, recipientId: String, deviceId: Int,
                               encryptedData: SignalEncryptedData): String {
        return try {
            signalClient.decryptMessage(recipientId = recipientId,
                    deviceId = deviceId,
                    encryptedData = encryptedData)
        } catch (ex: Exception) {
            if (ex is DuplicateMessageException) throw ex
            "Unable to decrypt message."
        }
    }

    private fun getDecryptedEmailBody(signalClient: SignalClient,
                                      body: String,
                                      metadata: EmailMetadata) =
        if (metadata.messageType != null && metadata.senderDeviceId != null) {
            val encryptedData = SignalEncryptedData(
                    encryptedB64 = body,
                    type = metadata.messageType)

            decryptMessage(signalClient = signalClient,
                    recipientId = metadata.senderRecipientId, deviceId = metadata.senderDeviceId,
                    encryptedData = encryptedData)
        } else body

    /**
     * Inserts all the rows and relations needed for a new email in a single transaction
     * @param apiClient Abstraction for the network calls needed
     * @param dao Abstraction for the database methods needed to insert all rows and relations
     * @param metadata metadata of the email to insert
     */
    fun insertIncomingEmailTransaction(signalClient: SignalClient, apiClient: EmailInsertionAPIClient,
                                       dao: EmailInsertionDao, metadata: EmailMetadata) {
        val labels = listOf(Label.defaultItems.inbox)

        val emailAlreadyExists = dao.findEmailByMessageId(metadata.messageId) != null
        if (emailAlreadyExists)
            throw DuplicateMessageException("Email Already exists in database!")

        val body = apiClient.getBodyFromEmail(metadata.messageId)

        val decryptedBody = getDecryptedEmailBody(signalClient, body, metadata)

        dao.runTransaction(Runnable {
                EmailInsertionSetup.exec(dao, metadata, decryptedBody, labels)
            })
    }
}
