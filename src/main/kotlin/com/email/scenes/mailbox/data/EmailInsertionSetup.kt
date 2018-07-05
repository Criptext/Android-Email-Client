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
    private fun createEmailRow(metadata: EmailMetadata.DBColumns, decryptedBody: String): Email {
        val preview = HTMLUtils.createEmailPreview(decryptedBody)
        return Email(
            id = 0,
            unread = metadata.unread,
            date = DateUtils.getDateFromString(
                    metadata.date,
                    null),
            threadId = metadata.threadId,
            subject = metadata.subject,
            secure = true,
            preview = preview,
            messageId = metadata.messageId,
            delivered = DeliveryTypes.NONE,
            content = decryptedBody,
            metadataKey = metadata.metadataKey
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
        val toAddressesNotDuplicated = toAddresses.toSet().toList()
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
        } else {
            dao.updateContactName(existingContacts.first().id, senderContact.name)
            existingContacts.first()
        }
    }

    private fun createFullEmailToInsert(dao: EmailInsertionDao,
                                        metadataColumns: EmailMetadata.DBColumns,
                                        decryptedBody: String,
                                        labels: List<Label>,
                                        files: List<CRFile>): FullEmail {
        val emailRow = createEmailRow(metadataColumns, decryptedBody)
        val senderContactRow = createSenderContactRow(dao, metadataColumns.fromContact)
        val toContactsRows = createContactRows(dao, metadataColumns.to)
        val ccContactsRows = createContactRows(dao, metadataColumns.cc)
        val bccContactsRows = createContactRows(dao, metadataColumns.bcc)
        return FullEmail(email = emailRow,
                to = toContactsRows,
                cc = ccContactsRows,
                bcc = bccContactsRows,
                labels = labels,
                from = senderContactRow, files = files)
    }

    private fun createEmailContactRelation(newEmailId: Long, type: ContactTypes)
            : (Contact) -> EmailContact =
            {
                contact -> EmailContact(id = 0, emailId = newEmailId,
                    contactId = contact.id, type = type)
            }

    private fun createEmailLabelRelation(newEmailId: Long): (Label) -> EmailLabel =
            { label -> EmailLabel(emailId = newEmailId, labelId = label.id) }

    private fun insertEmailLabelRelations(dao: EmailInsertionDao,
                                          fullEmail: FullEmail,
                                          newEmailId: Long) {
        val labelRelations = fullEmail.labels.map(createEmailLabelRelation(newEmailId))
        dao.insertEmailLabelRelations(labelRelations)
    }

    private fun insertEmailFiles(dao: EmailInsertionDao,
                                          fullEmail: FullEmail, emailId: Long) {
        fullEmail.files.forEach { it.emailId = emailId }
        dao.insertEmailFiles(fullEmail.files)
    }

    private fun insertEmailContactRelations(dao: EmailInsertionDao,
                                            fullEmail: FullEmail,
                                            newEmailId: Long) {
        val senderRelation = EmailContact(id = 0, emailId = newEmailId,
                contactId = fullEmail.from.id, type = ContactTypes.FROM)
        val toRelations = fullEmail.to.map(createEmailContactRelation(newEmailId, ContactTypes.TO))
        val ccRelations = fullEmail.cc.map(createEmailContactRelation(newEmailId, ContactTypes.CC))
        val bccRelations = fullEmail.bcc.map(
                createEmailContactRelation(newEmailId, ContactTypes.BCC))

        val contactRelations = listOf(toRelations, ccRelations, bccRelations)
                .flatten()
                .plus(senderRelation)

        dao.insertEmailContactRelations(contactRelations)
    }


    fun exec(dao: EmailInsertionDao,
             metadataColumns: EmailMetadata.DBColumns,
             decryptedBody: String,
             labels: List<Label>,
             files: List<CRFile>): Long {
        val fullEmail = createFullEmailToInsert(dao, metadataColumns, decryptedBody, labels, files)
        return exec(dao, fullEmail)
    }

    private fun exec(dao: EmailInsertionDao, fullEmail: FullEmail): Long {
        val newEmailId = dao.insertEmail(fullEmail.email)
        insertEmailLabelRelations(dao, fullEmail, newEmailId)
        insertEmailContactRelations(dao, fullEmail, newEmailId)
        insertEmailFiles(dao, fullEmail, newEmailId)
        return newEmailId
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
                                       dao: EmailInsertionDao, metadata: EmailMetadata, activeAccount: ActiveAccount) {
        val meAsSender = metadata.senderRecipientId == activeAccount.recipientId
        val meAsRecipient = metadata.bcc.contains(activeAccount.userEmail)
                || metadata.cc.contains(activeAccount.userEmail)
                || metadata.to.contains(activeAccount.userEmail)
        val labels = when {
            meAsSender && meAsRecipient -> listOf(Label.defaultItems.sent, Label.defaultItems.inbox)
            meAsSender -> listOf(Label.defaultItems.sent)
            else -> listOf(Label.defaultItems.inbox)
        }

        val emailAlreadyExists = dao.findEmailByMessageId(metadata.messageId) != null
        if (emailAlreadyExists)
            throw DuplicateMessageException("Email Already exists in database!")

        val body = apiClient.getBodyFromEmail(metadata.messageId)

        val decryptedBody = getDecryptedEmailBody(signalClient, body, metadata)

        dao.runTransaction({
                EmailInsertionSetup.exec(dao, metadata.extractDBColumns(), decryptedBody, labels, metadata.files)
            })
    }
}
