package com.criptext.mail.scenes.mailbox.data

import com.criptext.mail.api.EmailInsertionAPIClient
import com.criptext.mail.api.models.EmailMetadata
import com.criptext.mail.db.ContactTypes
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.dao.EmailInsertionDao
import com.criptext.mail.db.models.*
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalEncryptedData
import com.criptext.mail.signal.SignalUtils
import com.criptext.mail.utils.*
import com.criptext.mail.utils.file.FileUtils
import org.json.JSONObject
import org.whispersystems.libsignal.DuplicateMessageException
import java.io.File

/**
 * Runs the necessary steps before inserting an email like inserting all the contact rows to the
 * database.
 * Created by gabriel on 4/26/18.
 */
object  EmailInsertionSetup {
    private const val SPAM_COUNTER = 2

    private fun createEmailRow(metadata: EmailMetadata.DBColumns, preview: String, accountId: Long): Email {
        return Email(
            id = 0,
            fromAddress = if(metadata.fromContact.name.isEmpty()) metadata.fromContact.email
                else metadata.fromContact.name + " <${metadata.fromContact.email}>",
            replyTo = metadata.replyTo,
            boundary = metadata.boundary,
            unread = metadata.unread,
            date = DateAndTimeUtils.getDateFromString(
                    metadata.date,
                    null),
            unsentDate =  null,
            threadId = metadata.threadId,
            subject = metadata.subject,
            secure = metadata.secure,
            preview = preview,
            messageId = metadata.messageId,
            delivered = metadata.status,
            content = "",
            metadataKey = metadata.metadataKey,
            trashDate = null,
            accountId = accountId,
            isNewsletter = metadata.isNewsletter
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

        val toAddresses = addresses.map { EmailAddressUtils.extractEmailAddress(it) }
        val toNames = addresses.map { EmailAddressUtils.extractName(it) }

        val toContacts = mutableListOf<Contact>()
        for (i in 0..(toAddresses.size - 1))
            toContacts.add(Contact(id = 0, name = toNames[i], email = toAddresses[i], isTrusted = false, score = 0, spamScore = 0))
        val existingContacts = dao.findContactsByEmail(toContacts.toSet().map { it.email })

        val contactsMap = HashMap<String, Contact>()
        existingContacts.map { Pair(it.email, it) }.toMap(contactsMap)

        fillMapWithNewContacts(dao, contactsMap, toContacts.toSet().toList())

        return contactsMap.values.toList()
    }

    private fun createSenderContactRow(dao: EmailInsertionDao, senderContact: Contact, accountId: Long): Contact {
        val existingContacts = dao.findContactsByEmail(listOf(senderContact.email))
        return if (existingContacts.isEmpty()) {
            val ids = dao.insertContacts(listOf(senderContact))
            Contact(id = ids.first(),
                    name = EmailAddressUtils.extractName(senderContact.name),
                    email = EmailAddressUtils.extractEmailAddress(senderContact.email),
                    isTrusted = false,
                    score = senderContact.score,
                    spamScore = senderContact.spamScore)
        } else {
            dao.updateContactName(existingContacts.first().id, EmailAddressUtils.extractName(senderContact.name), accountId)
            existingContacts.first()
        }
    }

    private fun createFullEmailToInsert(dao: EmailInsertionDao,
                                        metadataColumns: EmailMetadata.DBColumns,
                                        preview: String,
                                        labels: List<Label>,
                                        files: List<CRFile>, fileKey: String?, accountId: Long): FullEmail {
        val senderContactRow = createSenderContactRow(dao, metadataColumns.fromContact, accountId)
        val emailRow = createEmailRow(metadataColumns, preview, accountId)
        val toContactsRows = createContactRows(dao, metadataColumns.to)
        val ccContactsRows = createContactRows(dao, metadataColumns.cc)
        val bccContactsRows = createContactRows(dao, metadataColumns.bcc)
        return FullEmail(email = emailRow,
                to = toContactsRows,
                cc = ccContactsRows,
                bcc = bccContactsRows,
                labels = labels,
                from = senderContactRow, files = files, fileKey = fileKey, headers = null)
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
        if(fileKey != null) {
            dao.insertEmailFileKey(FileKey(0, fileKey, emailId))
        }
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

    private fun insertAccountContactRelations(dao: EmailInsertionDao,
                                            fullEmail: FullEmail) {
        val senderRelation = AccountContact(id = 0,
                contactId = fullEmail.from.id, accountId = fullEmail.email.accountId)
        val toRelations = fullEmail.to.map{ AccountContact(id = 0,
                contactId = it.id, accountId = fullEmail.email.accountId)}
        val ccRelations = fullEmail.cc.map{ AccountContact(id = 0,
                contactId = it.id, accountId = fullEmail.email.accountId)}
        val bccRelations = fullEmail.bcc.map{ AccountContact(id = 0,
                contactId = it.id, accountId = fullEmail.email.accountId)}

        val contactRelations = listOf(toRelations, ccRelations, bccRelations)
                .flatten()
                .plus(senderRelation)

        dao.insertAccountContact(contactRelations)
    }


    fun exec(dao: EmailInsertionDao,
             metadataColumns: EmailMetadata.DBColumns,
             preview: String,
             labels: List<Label>,
             files: List<CRFile>, fileKey: String?, accountId: Long): Long {
        val fullEmail = createFullEmailToInsert(dao, metadataColumns, preview, labels,
                files, fileKey, accountId)
        return exec(dao, fullEmail)
    }

    private fun exec(dao: EmailInsertionDao, fullEmail: FullEmail): Long {
        val newEmailId = dao.insertEmail(fullEmail.email)
        insertEmailLabelRelations(dao, fullEmail, newEmailId)
        insertEmailContactRelations(dao, fullEmail, newEmailId)
        insertAccountContactRelations(dao, fullEmail)
        insertEmailFiles(dao, fullEmail, newEmailId)
        insertEmailFileKey(dao, fullEmail.fileKey, newEmailId)
        return newEmailId
    }

    private fun decryptMessage(signalClient: SignalClient, recipientId: String, deviceId: Int,
                               encryptedData: SignalEncryptedData, isFromBob: Boolean): String {
        return try {
            signalClient.decryptMessage(recipientId = recipientId,
                    deviceId = deviceId,
                    encryptedData = encryptedData)
        } catch (ex: Exception) {
            if (isFromBob) throw BobDecryptionException() else throw DecryptionException()
        }
    }

    private fun getSenderId(metadata: EmailMetadata): Pair<Int?, String>{
        val senderDeviceId = metadata.senderDeviceId
        var senderRecipientId = if(metadata.senderDomain.isEmpty() || metadata.senderDomain == Contact.mainDomain) metadata.senderRecipientId
        else metadata.senderRecipientId.plus("@${metadata.senderDomain}")
        if (metadata.isExternal != null && metadata.isExternal) {
            senderRecipientId = SignalUtils.externalRecipientId
        }
        return Pair(senderDeviceId, senderRecipientId)
    }

    fun getDecryptedEmailBody(signalClient: SignalClient,
                              body: String,
                              metadata: EmailMetadata): String {
        val senderId = getSenderId(metadata)
        return if (metadata.messageType != null && senderId.first != null) {
            val encryptedData = SignalEncryptedData(
                    encryptedB64 = body,
                    type = metadata.messageType)

            decryptMessage(signalClient = signalClient,
                    recipientId = senderId.second, deviceId = senderId.first!!,
                    encryptedData = encryptedData,
                    isFromBob = metadata.isExternal == true
                            && metadata.senderRecipientId == SignalUtils.externalRecipientId)
        } else
            body
    }

    fun getDecryptedEmailHeaders(signalClient: SignalClient,
                                 headers: String,
                                 metadata: EmailMetadata): String {
        val senderId = getSenderId(metadata)
        return if (metadata.messageType != null && senderId.first != null) {
            val encryptedData = SignalEncryptedData(
                    encryptedB64 = headers,
                    type = metadata.messageType)

            try {
                decryptMessage(signalClient = signalClient,
                        recipientId = senderId.second, deviceId = senderId.first!!,
                        encryptedData = encryptedData,
                        isFromBob = metadata.isExternal == true
                                && metadata.senderRecipientId == SignalUtils.externalRecipientId)
            } catch (e: Exception){
                headers
            }
        } else
            headers
    }

    private fun getDecryptedFileKey(signalClient: SignalClient,
                                      metadata: EmailMetadata): String? {
        val senderId = getSenderId(metadata)
        return if (metadata.messageType != null && senderId.first != null && metadata.fileKey != null) {
            val encryptedData = SignalEncryptedData(
                    encryptedB64 = metadata.fileKey,
                    type = metadata.messageType)

            decryptMessage(signalClient = signalClient,
                    recipientId = senderId.second, deviceId = senderId.first!!,
                    encryptedData = encryptedData,
                    isFromBob = metadata.isExternal == true
                            && metadata.senderRecipientId == SignalUtils.externalRecipientId)
        } else null
    }

    private fun getDecryptedFileKeys(signalClient: SignalClient,
                                    metadata: EmailMetadata): List<String> {
        val senderId = getSenderId(metadata)
        val fileKeys = mutableListOf<String>()

        metadata.files.forEach {
            when {
                metadata.messageType == null -> {
                    fileKeys.add(it.fileKey)
                }
                senderId.first != null && it.fileKey.isNotEmpty() -> {
                    val encryptedData = SignalEncryptedData(
                            encryptedB64 = it.fileKey,
                            type = metadata.messageType)

                    fileKeys.add(decryptMessage(signalClient = signalClient,
                            recipientId = senderId.second, deviceId = senderId.first!!,
                            encryptedData = encryptedData,
                            isFromBob = metadata.isExternal == true
                                    && metadata.senderRecipientId == SignalUtils.externalRecipientId))
                }
                else -> fileKeys.add("")
            }
        }
        return fileKeys
    }

    /**
     * Inserts all the rows and relations needed for a new email in a single transaction
     * @param apiClient Abstraction for the network calls needed
     * @param dao Abstraction for the database methods needed to insert all rows and relations
     * @param metadata metadata of the email to insert
     */
    fun insertIncomingEmailTransaction(signalClient: SignalClient, apiClient: EmailInsertionAPIClient,
                                       dao: EmailInsertionDao, metadata: EmailMetadata, activeAccount: ActiveAccount,
                                       filesDir: File, aliases: List<String>) {
        val foundEmail = dao.findEmailByMetadataKey(metadata.metadataKey, activeAccount.id)
        val emailAlreadyExists =  foundEmail != null
        if (emailAlreadyExists)
            throw DuplicateMessageException("Email Already exists in database!")

        val meAsSender = ((metadata.senderRecipientId == activeAccount.recipientId && metadata.senderDomain == activeAccount.domain)
                && (metadata.from.contains(activeAccount.userEmail))) || (EmailAddressUtils.extractEmailAddress(metadata.from) in aliases)
        val meAsRecipient = metadata.bcc.contains(activeAccount.userEmail)
                || metadata.cc.contains(activeAccount.userEmail)
                || metadata.to.contains(activeAccount.userEmail)
        val labels: MutableList<Label> = when {
            meAsSender && meAsRecipient -> mutableListOf(Label.defaultItems.sent, Label.defaultItems.inbox)
            meAsSender -> mutableListOf(Label.defaultItems.sent)
            else -> mutableListOf(Label.defaultItems.inbox)
        }

        if(!meAsSender) {
            val fromContact = dao.findContactsByEmail(listOf(metadata.fromContact.email)).firstOrNull()
            when {
                metadata.isSpam -> {
                    labels.add(Label.defaultItems.spam)
                }
                fromContact != null -> {
                    if(fromContact.spamScore >= SPAM_COUNTER){
                        labels.add(Label.defaultItems.spam)
                        apiClient.postReportSpam(listOf(fromContact.email),
                                ContactUtils.ContactReportTypes.spam, null)
                    }
                }
            }
        }

        val json = JSONObject(apiClient.getBodyFromEmail(metadata.metadataKey).body)
        val body = json.getString("body")
        val headers = json.optString("headers")

        val (decryptedBody, decryptedHeaders, decryptedFileKeys) =
            Triple(
                    HTMLUtils.sanitizeHtml(getDecryptedEmailBody(signalClient, body, metadata)),
                    if(headers.isNullOrEmpty()) null else getDecryptedEmailHeaders(signalClient, headers, metadata),
                    if(metadata.files.isEmpty())
                        listOf()
                    else getDecryptedFileKeys(signalClient, metadata)
            )

        val decryptedFileKey = if(decryptedFileKeys.isNotEmpty()) decryptedFileKeys[0] else getDecryptedFileKey(signalClient, metadata)

        metadata.files.forEachIndexed { index, crFile ->
            crFile.fileKey = decryptedFileKeys[index]
            crFile.cid = if (decryptedBody.contains("cid:${crFile.cid}")
                    && (FileUtils.isAPicture(crFile.name) || (!crFile.mimeType.isNullOrEmpty()
                            && crFile.mimeType!!.contains("image/")))) crFile.cid else null
        }


        val finalMetadata = if(metadata.inReplyTo != null) {
            val emailByMessageId = dao.findEmailByMessageId(metadata.inReplyTo, activeAccount.id)
            if(emailByMessageId != null)
                metadata.copy(
                        threadId = emailByMessageId.threadId
                )
            else
                metadata
        } else
            metadata


        EmailUtils.saveEmailInFileSystem(
                filesDir = filesDir,
                recipientId = activeAccount.recipientId,
                metadataKey = finalMetadata.metadataKey,
                content = decryptedBody,
                headers = decryptedHeaders,
                domain = activeAccount.domain)

        val lonReturn = dao.runTransaction {
            exec(dao, finalMetadata.extractDBColumns().copy(
                    unread = if(meAsSender && !meAsRecipient)
                                false
                             else
                                finalMetadata.extractDBColumns().unread,
                    status = if(meAsSender && meAsRecipient) DeliveryTypes.DELIVERED
            else if(meAsSender && !meAsRecipient)DeliveryTypes.SENT
            else DeliveryTypes.NONE), HTMLUtils.createEmailPreview(decryptedBody), labels,
                    finalMetadata.files, decryptedFileKey, activeAccount.id)
        }
        println(lonReturn)
    }

    class BobDecryptionException: Exception()
    class DecryptionException: Exception()
}
