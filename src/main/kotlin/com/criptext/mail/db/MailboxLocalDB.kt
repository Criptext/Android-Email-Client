package com.criptext.mail.db

import com.criptext.mail.db.models.*
import com.criptext.mail.scenes.mailbox.data.EmailThread
import com.criptext.mail.utils.EmailThreadValidator
import java.util.*

/**
 * Created by sebas on 1/26/18.
 */

interface MailboxLocalDB {

    fun createLabelEmailRelations(emailLabels: List<EmailLabel>)
    fun updateUnreadStatus(emailThreads: List<String>,
                           updateUnreadStatus: Boolean,
                           rejectedLabels: List<Long>)
    fun getCustomLabels(): List<Label>
    fun getCustomAndVisibleLabels(): List<Label>
    fun getLabelsFromThreadIds(threadIds: List<String>): List<Label>
    fun addEmail(email: Email) : Long
    fun createLabelsForEmailInbox(insertedEmailId: Long)
    fun getThreadsFromMailboxLabel(
            userEmail: String,
            labelName: String,
            startDate: Date?,
            limit: Int,
            rejectedLabels: List<Label>): List<EmailThread>

    fun getLabelsFromLabelType(labelNames: List<String>): List<Label>
    fun deleteRelationByEmailIds(emailIds: List<Long>)
    fun deleteRelationByLabelAndEmailIds(labelId: Long, emailIds: List<Long>)
    fun getLabelByName(labelName: String): Label
    fun getLabelsByName(labelName: List<String>): List<Label>
    fun updateEmailAndAddLabel(id: Long, threadId : String, messageId: String,
                               metadataKey: Long, date: Date, status: DeliveryTypes)
    fun getExistingAccount(): Account
    fun getUnreadCounterLabel(labelId: Long): Int
    fun setTrashDate(emailIds: List<Long>)
    fun getTotalCounterLabel(labelId: Long): Int
    fun getEmailsByThreadId(threadId: String, rejectedLabels: List<Long>): List<Email>
    fun getEmailById(id: Long): Email?
    fun getPendingEmails(deliveryTypes: List<Int>): List<FullEmail>
    fun deleteThreads(threadIds: List<String>)
    fun getEmailThreadFromEmail(email: Email, selectedLabel: String,
                                         rejectedLabels: List<Long>, userEmail: String): EmailThread
    fun getEmailThreadFromId(threadId: String, selectedLabel: String,
                                rejectedLabels: List<Long>, userEmail: String): EmailThread
    fun getThreadsIdsFromLabel(labelName: String): List<String>
    fun getEmailMetadataKeysFromLabel(labelName: String): List<Long>
    fun getExternalData(id: Long): EmailExternalSession?
    fun saveExternalSession(externalSession: EmailExternalSession)
    fun deleteEmail(emailId: List<Long>)


    class Default(private val db: AppDatabase): MailboxLocalDB {
        override fun getEmailById(id: Long): Email? {
            return db.emailDao().getEmailById(id)
        }

        override fun saveExternalSession(externalSession: EmailExternalSession) {
            db.emailExternalSessionDao().insert(externalSession)
        }

        override fun getExternalData(id: Long): EmailExternalSession? {
            return db.emailExternalSessionDao().getExternalSessionByEmailId(id)
        }

        override fun getPendingEmails(deliveryTypes: List<Int>): List<FullEmail> {
            val emails = db.emailDao().getPendingEmails(deliveryTypes,
                    Label.defaultItems.rejectedLabelsByMailbox(Label.defaultItems.inbox).map { it.id })
            val fullEmails =  emails.map {
                val id = it.id
                val labels = db.emailLabelDao().getLabelsFromEmail(id)
                val contactsCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.CC)
                val contactsBCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.BCC)
                val contactsFROM = db.emailContactDao().getContactsFromEmail(id, ContactTypes.FROM)
                val contactsTO = db.emailContactDao().getContactsFromEmail(id, ContactTypes.TO)
                val files = db.fileDao().getAttachmentsFromEmail(id)
                val fileKey = db.fileKeyDao().getAttachmentKeyFromEmail(id)

                FullEmail(
                        email = it,
                        bcc = contactsBCC,
                        cc = contactsCC,
                        from = contactsFROM[0],
                        files = files,
                        labels = labels,
                        to = contactsTO, fileKey = fileKey.key)
            }

            fullEmails.lastOrNull()?.viewOpen = true

            return fullEmails
        }

        override fun setTrashDate(emailIds: List<Long>) {
            db.emailDao().updateEmailTrashDate(Date(), emailIds)
        }

        override fun getThreadsIdsFromLabel(labelName: String): List<String> {
            val labelId = db.labelDao().get(labelName).id
            return db.emailDao().getThreadIdsFromLabel(labelId)
        }

        override fun getEmailMetadataKeysFromLabel(labelName: String): List<Long> {
            val labelId = db.labelDao().get(labelName).id
            return db.emailDao().getMetadataKeysFromLabel(labelId)
        }

        override fun getEmailThreadFromId(threadId: String, selectedLabel: String, rejectedLabels: List<Long>, userEmail: String): EmailThread {

            val email = db.emailDao().getEmailsFromThreadIds(listOf(threadId)).last()
            return emailThread(email, rejectedLabels, selectedLabel, userEmail)
        }

        override fun createLabelsForEmailInbox(insertedEmailId: Long) {
            val labelInbox = db.labelDao().get(Label.LABEL_INBOX)
            db.emailLabelDao().insert(EmailLabel(
                    labelId = labelInbox.id,
                    emailId = insertedEmailId))
        }

        override fun addEmail(email: Email): Long {
            return db.emailDao().insert(email)
        }

        override fun getCustomLabels(): List<Label>{
            return db.labelDao().getAllCustomLabels()
        }

        override fun getCustomAndVisibleLabels(): List<Label> {
            return db.labelDao().getCustomAndVisibleLabels()
        }

        override fun getLabelsFromThreadIds(threadIds: List<String>) : List<Label> {
            val labelSet = HashSet<Label>()
            threadIds.forEach {
                val labels = db.emailLabelDao().getLabelsFromEmailThreadId(it)
                labelSet.addAll(labels)
            }
            return labelSet.toList()
        }

        override fun createLabelEmailRelations(emailLabels: List<EmailLabel>){
            return db.emailLabelDao().insertAll(emailLabels)
        }

        private fun createLabelEmailSent(emailId: Long){
            db.emailLabelDao().insert(EmailLabel(
                    labelId = Label.defaultItems.sent.id,
                    emailId = emailId))
        }

        private fun createLabelEmailInbox(emailId: Long){
            db.emailLabelDao().insert(EmailLabel(
                    labelId = Label.defaultItems.inbox.id,
                    emailId = emailId))
        }

        override fun updateUnreadStatus(threadIds: List<String>,
                                        updateUnreadStatus: Boolean,
                                        rejectedLabels: List<Long>) {
            threadIds.forEach {
                val emailsIds = db.emailDao().getEmailsFromThreadId(it, rejectedLabels)
                        .map { email -> email.id }
                db.emailDao().toggleRead(ids = emailsIds, unread = updateUnreadStatus)
            }
        }

        override fun getEmailThreadFromEmail(email: Email, selectedLabel: String,
                                            rejectedLabels: List<Long>, userEmail: String): EmailThread {

            return emailThread(email, rejectedLabels, selectedLabel, userEmail)
        }

        override fun getThreadsFromMailboxLabel(userEmail: String, labelName: String,
                                                startDate: Date?, limit: Int,
                                                rejectedLabels: List<Label>): List<EmailThread> {

            val labels = db.labelDao().getAll()
            val selectedLabel = Label.getLabelIdWildcard(labelName, labels)
            val conditionalLabels = listOf(
                    Label.getLabelIdWildcard(Label.LABEL_TRASH, labels),
                    Label.getLabelIdWildcard(Label.LABEL_SPAM, labels)
            )
            val rejectedIdLabels = rejectedLabels.filter {label ->
                label.text != labelName
            }.map {
                it.id
            }
            val emails = if(startDate != null)
                db.emailDao().getEmailThreadsFromMailboxLabel(
                        isTrashOrSpam = (selectedLabel in conditionalLabels),
                        startDate = startDate,
                        rejectedLabels = rejectedIdLabels,
                        selectedLabel = selectedLabel,
                        limit = limit )

            else
                db.emailDao().getInitialEmailThreadsFromMailboxLabel(
                        isTrashOrSpam = (selectedLabel in conditionalLabels),
                        rejectedLabels = rejectedIdLabels,
                        selectedLabel = selectedLabel,
                        limit = limit )

            return if (emails.isNotEmpty()){
                emails.map { email ->
                    getEmailThreadFromEmail(email, labelName,
                            Label.defaultItems.rejectedLabelsByMailbox(
                                    db.labelDao().get(labelName)
                            ).map { it.id }, userEmail)
                } as ArrayList<EmailThread>
            }else emptyList()
        }

        override fun getLabelsFromLabelType(labelNames: List<String>): List<Label> {
            return db.labelDao().get(labelNames)
        }

        override fun getLabelByName(labelName: String): Label {
            return db.labelDao().get(labelName)
        }

        override fun getLabelsByName(labelName: List<String>): List<Label> {
            return db.labelDao().get(labelName)
        }

        override fun deleteRelationByEmailIds(emailIds: List<Long>) {
            db.emailLabelDao().deleteRelationByEmailIds(emailIds)
        }

        override fun deleteRelationByLabelAndEmailIds(labelId: Long, emailIds: List<Long>){
            db.emailLabelDao().deleteRelationByLabelAndEmailIds(labelId, emailIds)
        }

        override fun deleteEmail(emailId: List<Long>) {
            db.emailDao().deleteByIds(emailId)
        }

        private fun updateEmail(id: Long, threadId: String, messageId : String, metadataKey: Long,
                        date: Date, status: DeliveryTypes) {
            db.emailDao().updateEmail(id = id, threadId = threadId, messageId = messageId,
                    metadataKey = metadataKey, date = date, status = status)
        }

        override fun updateEmailAndAddLabel(id: Long, threadId: String, messageId: String,
                                            metadataKey: Long, date: Date, status: DeliveryTypes) {
            db.runInTransaction({
                updateEmail(id = id, threadId = threadId, messageId = messageId,
                        metadataKey = metadataKey, date = date, status = status)
                deleteRelationByEmailIds(arrayListOf(id))
                createLabelEmailSent(id)
                if(status == DeliveryTypes.DELIVERED)
                    createLabelEmailInbox(id)
            })
        }

        override fun getExistingAccount(): Account {
            return db.accountDao().getLoggedInAccount()!!
        }

        override fun getUnreadCounterLabel(labelId: Long): Int {
            val rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(
                    db.labelDao().getLabelById(labelId)).map { it.id }
            return db.emailDao().getTotalUnreadThreads(rejectedLabels, "%$labelId%").size
        }

        override fun getTotalCounterLabel(labelId: Long): Int {
            return db.emailDao().getTotalThreads("%$labelId%").size
        }

        override fun getEmailsByThreadId(threadId: String, rejectedLabels: List<Long>): List<Email> {
            return db.emailDao().getEmailsFromThreadId(threadId, rejectedLabels)
        }

        override fun deleteThreads(threadIds: List<String>) {
            db.emailDao().deleteThreads(threadIds)
        }

        private fun emailThread(email: Email, rejectedLabels: List<Long>, selectedLabel: String, userEmail: String): EmailThread {
            val id = email.id
            val labels = db.emailLabelDao().getLabelsFromEmail(id)
            val emailsInSelectedLabel = if(selectedLabel != Label.LABEL_ALL_MAIL)
                db.emailLabelDao().getEmailCountInLabelByEmailId(email.threadId,
                        db.labelDao().get(selectedLabel).id) else -1
            val contactsCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.CC)
            val contactsBCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.BCC)
            val contactsFROM = db.emailContactDao().getContactsFromEmail(id, ContactTypes.FROM)
            val contactsTO = db.emailContactDao().getContactsFromEmail(id, ContactTypes.TO)
            val files = db.fileDao().getAttachmentsFromEmail(id)
            val fileKey = db.fileKeyDao().getAttachmentKeyFromEmail(id)
            email.subject = email.subject.replace("^(Re|RE): ".toRegex(), "")
                    .replace("^(Fw|FW|Fwd|FWD): ".toRegex(), "")

            val emails = db.emailDao().getEmailsFromThreadId(email.threadId, rejectedLabels)
            var totalFiles = 0
            val participants = emails.flatMap {
                val contacts = mutableListOf<Contact>()
                if (selectedLabel == Label.defaultItems.sent.text) {
                    val emailLabels = db.emailLabelDao().getLabelsFromEmail(it.id)
                    if (EmailThreadValidator.isLabelInList(emailLabels, Label.LABEL_SENT)) {
                        contacts.addAll(db.emailContactDao().getContactsFromEmail(it.id, ContactTypes.TO))
                        contacts.addAll(db.emailContactDao().getContactsFromEmail(it.id, ContactTypes.CC))
                    }
                } else {
                    contacts.addAll(db.emailContactDao().getContactsFromEmail(it.id, ContactTypes.FROM))
                }
                contacts.map { contact ->
                    if (contact.email == userEmail) {
                        //It's difficult to reach String resources, so I will leave the 'me' string for now
                        contact.name = "me"
                    }
                }
                totalFiles += db.fileDao().getAttachmentsFromEmail(it.id).size
                contacts
            }

            return EmailThread(
                    participants = participants.distinctBy { it.id },
                    currentLabel = selectedLabel,
                    latestEmail = FullEmail(
                            email = email,
                            bcc = contactsBCC,
                            cc = contactsCC,
                            from = contactsFROM[0],
                            files = files,
                            labels = labels,
                            to = contactsTO,
                            fileKey = fileKey.key),
                    totalEmails = if(emailsInSelectedLabel == -1)
                        emails.size else emailsInSelectedLabel,
                    hasFiles = totalFiles > 0
            )
        }
    }

}
