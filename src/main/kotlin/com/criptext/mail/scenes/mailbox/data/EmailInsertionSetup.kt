package com.criptext.mail.scenes.mailbox.data

import com.criptext.mail.api.EmailInsertionAPIClient
import com.criptext.mail.api.models.EmailMetadata
import com.criptext.mail.db.ContactTypes
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.dao.EmailInsertionDao
import com.criptext.mail.db.models.*
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalEncryptedData
import com.criptext.mail.utils.DateUtils
import com.criptext.mail.utils.HTMLUtils
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
            unsentDate =  DateUtils.getDateFromString(
                    metadata.date,
                    null),
            threadId = metadata.threadId,
            subject = metadata.subject,
            secure = metadata.secure,
            preview = preview,
            messageId = metadata.messageId,
            delivered = metadata.status,
            content = decryptedBody,
            metadataKey = metadata.metadataKey,
            isMuted = false
        )
    }

    private fun fillMapWithNewContacts(dao: EmailInsertionDao, contactsMap: HashMap<String, Contact>,
                                       toAddresses: List<Contact>) {
        val unknownContacts = toAddresses.filter { !contactsMap.containsKey(it.email) }
                .map { it }

        if (unknownContacts.isNotEmpty()) {
            val insertedContactIds = dao.insertContacts(unknownContacts)

            unknownContacts.forEachIndexed { index, contact ->
                contact.id = insertedContactIds[index]
                contactsMap[contact.email] = contact
            }
        }
    }

    private fun createContactRows(dao: EmailInsertionDao, addresses: List<String>): List<Contact> {
        if (addresses.isEmpty()) return emptyList()

        val toAddresses = addresses.map { removeEmailInvalidCharacters(it) }
        val toNames = addresses.map { removeNameInvalidCharacters(it) }

        val toContacts = mutableListOf<Contact>()
        for (i in 0..(toAddresses.size - 1))
            toContacts.add(Contact(id = 0, name = toNames[i], email = toAddresses[i]))
        val existingContacts = dao.findContactsByEmail(toContacts.toSet().map { it.email })

        val contactsMap = HashMap<String, Contact>()
        existingContacts.map { Pair(it.email, it) }.toMap(contactsMap)

        fillMapWithNewContacts(dao, contactsMap, toContacts.toSet().toList())

        return contactsMap.values.toList()
    }

    private fun removeEmailInvalidCharacters(email: String): String{
        val realEmail = if(email.contains("<") && email.contains(">"))
            email.substring(email.lastIndexOf("<") + 1, email.lastIndexOf(">")) else email
        return realEmail.replace("<", "")
                .replace(">", "")
                .replace("\"", "")
                .toLowerCase()
    }

    private fun removeNameInvalidCharacters(name: String): String{
        val realName = if(name.contains("<") && name.contains(">"))
                                name.substring(0, name.lastIndexOf("<") - 1)
                                else if(name.contains("@")) name.split("@")[0]
                                else name
        return realName.replace("\"", "")
                .replace("<", "")
                .replace(">", "")
    }

    private fun createSenderContactRow(dao: EmailInsertionDao, senderContact: Contact): Contact {
        val existingContacts = dao.findContactsByEmail(listOf(senderContact.email))
        return if (existingContacts.isEmpty()) {
            val ids = dao.insertContacts(listOf(senderContact))
            Contact(id = ids.first(),
                    name = removeNameInvalidCharacters(senderContact.name),
                    email = removeEmailInvalidCharacters(senderContact.email))
        } else {
            dao.updateContactName(existingContacts.first().id, removeNameInvalidCharacters(senderContact.name))
            existingContacts.first()
        }
    }

    private fun createFullEmailToInsert(dao: EmailInsertionDao,
                                        metadataColumns: EmailMetadata.DBColumns,
                                        decryptedBody: String,
                                        labels: List<Label>,
                                        files: List<CRFile>, fileKey: String?): FullEmail {
        val senderContactRow = createSenderContactRow(dao, metadataColumns.fromContact)
        val emailRow = createEmailRow(metadataColumns, decryptedBody)
        val toContactsRows = createContactRows(dao, metadataColumns.to)
        val ccContactsRows = createContactRows(dao, metadataColumns.cc)
        val bccContactsRows = createContactRows(dao, metadataColumns.bcc)
        return FullEmail(email = emailRow,
                to = toContactsRows,
                cc = ccContactsRows,
                bcc = bccContactsRows,
                labels = labels,
                from = senderContactRow, files = files, fileKey = fileKey)
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

    private fun insertEmailFileKey(dao: EmailInsertionDao, fileKey: String?, emailId: Long) {
        dao.insertEmailFileKey(FileKey(0, fileKey, emailId))
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
             files: List<CRFile>, fileKey: String?): Long {
        val fullEmail = createFullEmailToInsert(dao, metadataColumns, decryptedBody, labels,
                files, fileKey)
        return exec(dao, fullEmail)
    }

    private fun exec(dao: EmailInsertionDao, fullEmail: FullEmail): Long {
        val newEmailId = dao.insertEmail(fullEmail.email)
        insertEmailLabelRelations(dao, fullEmail, newEmailId)
        insertEmailContactRelations(dao, fullEmail, newEmailId)
        insertEmailFiles(dao, fullEmail, newEmailId)
        insertEmailFileKey(dao, fullEmail.fileKey, newEmailId)
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

    private fun getDecryptedFileKey(signalClient: SignalClient,
                                      metadata: EmailMetadata) =
            if (metadata.messageType != null && metadata.senderDeviceId != null && metadata.fileKey != null) {
                val encryptedData = SignalEncryptedData(
                        encryptedB64 = metadata.fileKey,
                        type = metadata.messageType)

                decryptMessage(signalClient = signalClient,
                        recipientId = metadata.senderRecipientId, deviceId = metadata.senderDeviceId,
                        encryptedData = encryptedData)
            } else null


    /**
     * Inserts all the rows and relations needed for a new email in a single transaction
     * @param apiClient Abstraction for the network calls needed
     * @param dao Abstraction for the database methods needed to insert all rows and relations
     * @param metadata metadata of the email to insert
     */
    fun insertIncomingEmailTransaction(signalClient: SignalClient, apiClient: EmailInsertionAPIClient,
                                       dao: EmailInsertionDao, metadata: EmailMetadata, activeAccount: ActiveAccount) {
        val meAsSender = (metadata.senderRecipientId == activeAccount.recipientId)
                && (metadata.from.contains(activeAccount.userEmail))
        val meAsRecipient = metadata.bcc.contains(activeAccount.userEmail)
                || metadata.cc.contains(activeAccount.userEmail)
                || metadata.to.contains(activeAccount.userEmail)
        val labels = when {
            meAsSender && meAsRecipient -> listOf(Label.defaultItems.sent, Label.defaultItems.inbox)
            meAsSender -> listOf(Label.defaultItems.sent)
            else -> listOf(Label.defaultItems.inbox)
        }

        val foundEmail = dao.findEmailByMessageId(metadata.messageId)
        val emailAlreadyExists =  foundEmail?.messageId == metadata.messageId
        if (emailAlreadyExists)
            throw DuplicateMessageException("Email Already exists in database!")

        val body = apiClient.getBodyFromEmail(metadata.metadataKey)

        val decryptedBody = getDecryptedEmailBody(signalClient, body, metadata)

        val decryptedFileKey = getDecryptedFileKey(signalClient, metadata)

        val lonReturn = dao.runTransaction({
                EmailInsertionSetup.exec(dao, metadata.extractDBColumns().copy(unread =
                if(meAsSender) false else metadata.extractDBColumns().unread, status =
                if(meAsSender) DeliveryTypes.SENT else metadata.extractDBColumns().status), decryptedBody, labels,
                        metadata.files, decryptedFileKey)
            })
        println(lonReturn)
    }
}
