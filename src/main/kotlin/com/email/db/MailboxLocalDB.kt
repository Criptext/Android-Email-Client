package com.email.db

import com.email.SecureEmail
import com.email.db.models.*
import com.email.db.typeConverters.LabelTextConverter
import com.email.email_preview.EmailPreview
import com.email.scenes.mailbox.data.EmailThread
import com.email.utils.EmailThreadValidator
import com.github.kittinunf.result.Result
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
    fun getLabelsFromThreadIds(threadIds: List<String>): List<Label>
    fun addEmail(email: Email) : Long
    fun createLabelsForEmailInbox(insertedEmailId: Long)
    fun getThreadsFromMailboxLabel(
            userEmail: String,
            labelTextTypes: MailFolders,
            startDate: Date?,
            limit: Int,
            rejectedLabels: List<Label>): List<EmailThread>

    fun getLabelsFromLabelType(labelTextTypes: List<MailFolders>): List<Label>
    fun deleteRelationByEmailIds(emailIds: List<Long>)
    fun deleteRelationByLabelAndEmailIds(labelId: Long, emailIds: List<Long>)
    fun getLabelFromLabelType(labelTextType: MailFolders): Label
    fun updateEmailAndAddLabelSent(id: Long, threadId : String, messageId: String,
                                   metadataKey: Long, date: Date, status: DeliveryTypes)
    fun getExistingAccount(): Account
    fun getUnreadCounterLabel(labelId: Long): Int
    fun getTotalCounterLabel(labelId: Long): Int
    fun getEmailsByThreadId(threadId: String, rejectedLabels: List<Long>): List<Email>
    fun deleteThreads(threadIds: List<String>)

    class Default(private val db: AppDatabase): MailboxLocalDB {
        override fun createLabelsForEmailInbox(insertedEmailId: Long) {
            val labelInbox = db.labelDao().get(MailFolders.INBOX)
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

        override fun updateUnreadStatus(threadIds: List<String>,
                                        updateUnreadStatus: Boolean,
                                        rejectedLabels: List<Long>) {
            threadIds.forEach {
                val emailsIds = db.emailDao().getEmailsFromThreadId(it, rejectedLabels)
                        .map { email -> email.id }
                db.emailDao().toggleRead(ids = emailsIds, unread = updateUnreadStatus)
            }
        }

        private fun getEmailThreadFromEmail(email: Email, selectedLabel: MailFolders,
                                            rejectedLabels: List<Long>, userEmail: String): EmailThread {

            val id = email.id
            val labels = db.emailLabelDao().getLabelsFromEmail(id)
            val contactsCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.CC)
            val contactsBCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.BCC)
            val contactsFROM = db.emailContactDao().getContactsFromEmail(id, ContactTypes.FROM)
            val contactsTO = db.emailContactDao().getContactsFromEmail(id, ContactTypes.TO)
            val files = db.fileDao().getAttachmentsFromEmail(id)
            email.subject = email.subject.replace("^(Re|RE): ".toRegex(), "")
                    .replace("^(Fw|FW): ".toRegex(), "")

            val emails = db.emailDao().getEmailsFromThreadId(email.threadId, rejectedLabels)
            var totalFiles = 0
            val participants = emails.flatMap {
                val contacts = mutableListOf<Contact>()
                if(selectedLabel == Label.defaultItems.sent.text){
                    val emailLabels = db.emailLabelDao().getLabelsFromEmail(it.id)
                    if(EmailThreadValidator.isLabelInList(emailLabels, SecureEmail.LABEL_SENT)){
                        contacts.addAll(db.emailContactDao().getContactsFromEmail(it.id, ContactTypes.TO))
                        contacts.addAll(db.emailContactDao().getContactsFromEmail(it.id, ContactTypes.CC))
                    }
                }
                else{
                    contacts.addAll(db.emailContactDao().getContactsFromEmail(it.id, ContactTypes.FROM))
                }
                contacts.map { contact ->
                    if(contact.email == userEmail){
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
                            to = contactsTO ),
                    totalEmails = emails.size,
                    hasFiles = totalFiles > 0
            )
        }

        override fun getThreadsFromMailboxLabel(userEmail: String, labelTextTypes: MailFolders,
                                                startDate: Date?, limit: Int,
                                                rejectedLabels: List<Label>): List<EmailThread> {

            val labels = db.labelDao().getAll()
            val selectedLabel = if(labelTextTypes == MailFolders.ALL_MAIL) "%" else
                "%${labels.findLast {
                    label ->label.text == labelTextTypes
                }?.id}%"
            val rejectedIdLabels = rejectedLabels.filter {label ->
                label.text != labelTextTypes
            }.map {
                it.id
            }
            val emails = if(startDate != null)
                db.emailDao().getEmailThreadsFromMailboxLabel(
                        startDate = startDate,
                        rejectedLabels = rejectedIdLabels,
                        selectedLabel = selectedLabel,
                        limit = limit )

            else
                db.emailDao().getInitialEmailThreadsFromMailboxLabel(
                        rejectedLabels = rejectedIdLabels,
                        selectedLabel = selectedLabel,
                        limit = limit )

            return emails.map { email ->
                getEmailThreadFromEmail(email, labelTextTypes,
                        Label.defaultItems.rejectedLabelsByMailbox(
                                db.labelDao().get(labelTextTypes)
                        ).map { it.id }, userEmail)
            } as ArrayList<EmailThread>
        }

        override fun getLabelsFromLabelType(labelTextTypes: List<MailFolders>): List<Label> {
            val stringLabelTypes = labelTextTypes.map { labelTextType->
                LabelTextConverter().parseLabelTextType(labelTextType)
            }
            return db.labelDao().get(stringLabelTypes)
        }

        override fun getLabelFromLabelType(labelTextType: MailFolders): Label {
            return db.labelDao().get(labelTextType)
        }

        override fun deleteRelationByEmailIds(emailIds: List<Long>) {
            db.emailLabelDao().deleteRelationByEmailIds(emailIds)
        }

        override fun deleteRelationByLabelAndEmailIds(labelId: Long, emailIds: List<Long>){
            db.emailLabelDao().deleteRelationByLabelAndEmailIds(labelId, emailIds)
        }

        private fun updateEmail(id: Long, threadId: String, messageId : String, metadataKey: Long,
                        date: Date, status: DeliveryTypes) {
            db.emailDao().updateEmail(id = id, threadId = threadId, messageId = messageId,
                    metadataKey = metadataKey, date = date, status = status)
        }

        override fun updateEmailAndAddLabelSent(id: Long, threadId: String, messageId: String,
                                                metadataKey: Long, date: Date, status: DeliveryTypes) {
            db.runInTransaction({
                updateEmail(id = id, threadId = threadId, messageId = messageId,
                        metadataKey = metadataKey, date = date, status = status)
                deleteRelationByEmailIds(arrayListOf(id))
                createLabelEmailSent(id)
            })
        }

        override fun getExistingAccount(): Account {
            return db.accountDao().getLoggedInAccount()!!
        }

        override fun getUnreadCounterLabel(labelId: Long): Int {
            return db.emailDao().getTotalUnreadThreads(emptyList(), "%$labelId%").size
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
    }

}
