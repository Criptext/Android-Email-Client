package com.email.db

import com.email.db.models.*
import com.email.db.typeConverters.LabelTextConverter
import com.email.scenes.mailbox.data.EmailThread
import com.github.kittinunf.result.Result
import java.util.*

/**
 * Created by sebas on 1/26/18.
 */

interface MailboxLocalDB {

    fun createLabelEmailRelations(emailLabels: List<EmailLabel>)
    fun updateUnreadStatus(emailThreads: List<EmailThread>,
                           updateUnreadStatus: Boolean,
                           rejectedLabels: List<Long>)
    fun getCustomLabels(): List<Label>
    fun getLabelsFromThreadIds(threadIds: List<String>): List<Label>
    fun addEmail(email: Email) : Long
    fun createLabelsForEmailInbox(insertedEmailId: Long)
    fun getEmailsFromMailboxLabel(
            labelTextTypes: MailFolders,
            oldestEmailThread: EmailThread?,
            limit: Int,
            rejectedLabels: List<Label>): List<EmailThread>

    fun getLabelsFromLabelType(labelTextTypes: List<MailFolders>): List<Label>
    fun deleteRelationByEmailIds(emailIds: List<Long>)
    fun deleteRelationByLabelAndEmailIds(labelId: Long, emailIds: List<Long>)
    fun getLabelFromLabelType(labelTextType: MailFolders): Label
    fun updateEmailAndAddLabelSent(id: Long, threadId : String, key : String, date: Date, status: DeliveryTypes)
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

        override fun updateUnreadStatus(emailThreads: List<EmailThread>,
                                        updateUnreadStatus: Boolean,
                                        rejectedLabels: List<Long>) {
            emailThreads.forEach {
                val emailsIds = db.emailDao().getEmailsFromThreadId(it.threadId, rejectedLabels)
                        .map {
                    it.id
                }
                db.emailDao().toggleRead(ids = emailsIds, unread = updateUnreadStatus)
            }
        }

        private fun getEmailThreadFromEmail(email: Email, rejectedLabels: List<Long>): EmailThread {
            val id = email.id
            val labels = db.emailLabelDao().getLabelsFromEmail(id)
            val contactsCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.CC)
            val contactsBCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.BCC)
            val contactsFROM = db.emailContactDao().getContactsFromEmail(id, ContactTypes.FROM)
            val contactsTO = db.emailContactDao().getContactsFromEmail(id, ContactTypes.TO)
            val files = db.fileDao().getAttachmentsFromEmail(id)
            val totalEmails = db.emailDao().getTotalEmailsByThread(email.threadId, rejectedLabels)
            email.subject = email.subject.replace("^(Re|RE): ".toRegex(), "")
                    .replace("^(Fw|FW): ".toRegex(), "")

            return EmailThread(
                    latestEmail = FullEmail(
                            email = email,
                            bcc = contactsBCC,
                            cc = contactsCC,
                            from = contactsFROM[0],
                            files = files,
                            labels = labels,
                            to = contactsTO ),
                    totalEmails = totalEmails,
                    labelsOfMail = db.emailLabelDao().getLabelsFromEmail(email.id) as ArrayList<Label>
            )

        }

        override fun getEmailsFromMailboxLabel(
                labelTextTypes: MailFolders,
                oldestEmailThread: EmailThread?,
                limit: Int,
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
            val emails = if(oldestEmailThread != null)
                db.emailDao().getEmailThreadsFromMailboxLabel(
                        starterDate = oldestEmailThread.timestamp,
                        rejectedLabels = rejectedIdLabels,
                        selectedLabel = selectedLabel,
                        limit = limit )

            else
                db.emailDao().getInitialEmailThreadsFromMailboxLabel(
                        rejectedLabels = rejectedIdLabels,
                        selectedLabel = selectedLabel,
                        limit = limit )

            return emails.map { email ->
                getEmailThreadFromEmail(email,
                        Label.defaultItems.rejectedLabelsByMailbox(
                                db.labelDao().get(labelTextTypes)
                        ).map { it.id })
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

        fun updateEmail(id: Long, threadId: String, key : String, date: Date, status: DeliveryTypes){
            db.emailDao().updateEmail(id, threadId, key, date, status)
        }

        override fun updateEmailAndAddLabelSent(id: Long, threadId : String, key : String, date: Date, status: DeliveryTypes){
            db.runInTransaction({
                updateEmail(id, threadId, key, date, status)
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
