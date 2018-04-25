package com.email.db

import com.email.db.models.*
import com.email.db.typeConverters.LabelTextConverter
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.labelChooser.data.LabelWrapper
import com.github.kittinunf.result.Result
import java.util.*

/**
 * Created by sebas on 1/26/18.
 */

interface MailboxLocalDB {

    fun getAllEmailThreads(): List<EmailThread>
    fun getArchivedEmailThreads(): List<EmailThread>
    fun getAllLabelWrappers(): List<LabelWrapper>
    fun getAllLabels(): List<Label>
    fun getNotArchivedEmailThreads(): List<EmailThread>
    fun removeLabelsRelation(labels: List<Label>, emailId: Long)
    fun deleteEmailThreads(emailThreads: List<EmailThread>)
    fun createLabelEmailRelation(labelId: Long, emailId: Long)
    fun createLabelEmailSent(emailId: Long)
    fun updateUnreadStatus(emailThreads: List<EmailThread>,
                           updateUnreadStatus: Boolean)
    fun moveSelectedEmailThreadsToSpam(emailThreads: List<EmailThread>)
    fun moveSelectedEmailThreadsToTrash(emailThreads: List<EmailThread>)
    fun getLabelsFromThreadIds(threadIds: List<String>): List<Label>
    fun addEmail(email: Email) : Long
    fun createLabelsForEmailInbox(insertedEmailId: Long)
    fun createContacts(contactName: String?, contactEmail: String, insertedEmailId: Long, type: ContactTypes)
    fun getEmailsFromMailboxLabel(
            labelTextTypes: MailFolders,
            oldestEmailThread: EmailThread?,
            offset: Int,
            rejectedLabels: List<Label>): List<EmailThread>

    fun getLabelsFromLabelType(labelTextTypes: List<MailFolders>): List<Label>
    fun deleteRelationByEmailIds(emailIds: List<Long>)
    fun getLabelFromLabelType(labelTextType: MailFolders): Label
    fun updateEmailAndAddLabelSent(id: Long, threadId : String, key : String, date: Date, status: DeliveryTypes)
    fun getExistingAccount(): Account
    fun getUnreadCounterLabel(labelId: Long): Int
    fun getTotalCounterLabel(labelId: Long): Int

    val getEmailThreadOperation: (threadId: String) -> Result<EmailThread, Exception>

    class Default(private val db: AppDatabase): MailboxLocalDB {
        override fun createLabelsForEmailInbox(insertedEmailId: Long) {
            val labelInbox = db.labelDao().get(MailFolders.INBOX)
            db.emailLabelDao().insert(EmailLabel(
                    labelId = labelInbox.id,
                    emailId = insertedEmailId))
        }

        private fun insertContact(contactName: String?, contactEmail: String, emailId: Long,
                                  type: ContactTypes) {
            if(contactEmail.isNotEmpty()) {
                val contact = Contact(id = 0, email = contactEmail, name = contactName ?: contactEmail)
                var contactId = db.contactDao().insertIgnoringConflicts(contact)
                if(contactId < 0){
                    contactId = db.contactDao().getContact(contactEmail)!!.id
                }
                val emailContact = EmailContact(
                        contactId = contactId,
                        emailId = emailId,
                        type = type)
                db.emailContactDao().insert(emailContact)
            }
        }

        override fun addEmail(email: Email): Long {
            return db.emailDao().insert(email)
        }

        override fun getLabelsFromThreadIds(threadIds: List<String>) : List<Label> {
            val labelSet = HashSet<Label>()

            threadIds.forEach {
                    val labels = db.
                            emailLabelDao().
                            getLabelsFromEmailThreadId(it)
                    labelSet.addAll(labels)
            }

            return labelSet.toList()
        }

        override fun createLabelEmailRelation(labelId: Long, emailId: Long) {
            val emailLabel = EmailLabel(labelId = labelId, emailId = emailId)
            return db.emailLabelDao().insert(emailLabel)
        }

        override fun createLabelEmailSent(emailId: Long){
            createLabelEmailRelation(Label.defaultItems.sent.id, emailId)
        }

        override fun getAllEmailThreads(): List<EmailThread> {
            return db.emailDao().getAll().map { email ->
                getEmailThreadFromEmail(email)
            } as ArrayList<EmailThread>
        }

        override fun getArchivedEmailThreads(): List<EmailThread> {
            return db.emailDao().getAll().map { email ->
                getEmailThreadFromEmail(email)
            } as ArrayList<EmailThread>
        }

        override fun getNotArchivedEmailThreads(): List<EmailThread> {
            return db.emailDao().getNotArchivedEmailThreads().map { email ->
                getEmailThreadFromEmail(email)
            }
        }

        override fun removeLabelsRelation(labels: List<Label>, emailId: Long) {
            labels.forEach{
                db.emailLabelDao().deleteByEmailLabelIds(it.id, emailId)
            }
        }

        override fun deleteEmailThreads(emailThreads: List<EmailThread>) {
            val emails: List<Email> = emailThreads.map {
                it.latestEmail.email
            }
            db.emailDao().deleteAll(emails)
        }

        override fun getAllLabelWrappers(): List<LabelWrapper> {
            return db.labelDao().getAll().map{ label ->
                LabelWrapper(label)
            } as ArrayList<LabelWrapper>
        }

        override fun getAllLabels(): List<Label> {
            return db.labelDao().getAll()
        }

        override fun updateUnreadStatus(emailThreads: List<EmailThread>, updateUnreadStatus: Boolean) {
            emailThreads.forEach {
                db.emailDao().toggleRead(id = it.latestEmail.email.id,
                        unread = updateUnreadStatus)
            }
        }

        override fun moveSelectedEmailThreadsToSpam(emailThreads: List<EmailThread>) {
            TODO("MOVE EMAILS TO SPAM")
        }

        override fun moveSelectedEmailThreadsToTrash(emailThreads: List<EmailThread>) {
            val emails = emailThreads.map {
                val trashLabelId = Label.defaultItems.trash.id
                db.emailLabelDao().insert(
                        EmailLabel(
                                emailId = it.id,
                                labelId = trashLabelId))

                it.latestEmail.email.isTrash = true
                    it.latestEmail.email
                }

            db.emailDao().update(emails)
        }

        private fun getEmailThreadFromEmail(email: Email): EmailThread {
            val id = email.id
            val labels = db.emailLabelDao().getLabelsFromEmail(id)
            val contactsCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.CC)
            val contactsBCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.BCC)
            val contactsFROM = db.emailContactDao().getContactsFromEmail(id, ContactTypes.FROM)
            val contactsTO = db.emailContactDao().getContactsFromEmail(id, ContactTypes.TO)
            val files = db.fileDao().getAttachmentsFromEmail(id)

            return EmailThread(
                    latestEmail = FullEmail(
                            email = email,
                            bcc = contactsBCC,
                            cc = contactsCC,
                            from = contactsFROM[0],
                            files = files,
                            labels = labels,
                            to = contactsTO ),
                    labelsOfMail = db.emailLabelDao().getLabelsFromEmail(email.id) as ArrayList<Label>
            )

        }

        override fun createContacts(contactName: String?, contactEmail: String,
                                    insertedEmailId: Long, type: ContactTypes) {
            if(type == ContactTypes.FROM) {
                insertContact(
                        contactName = contactName,
                        contactEmail = contactEmail,
                        emailId = insertedEmailId,
                        type = type)
               return
            }

            val contactsList = contactEmail.split(",")
            contactsList.forEach { email ->
                insertContact(
                        contactName = contactName,
                        contactEmail = email,
                        emailId = insertedEmailId,
                        type = type)
            }
        }

        override fun getEmailsFromMailboxLabel(
                labelTextTypes: MailFolders,
                oldestEmailThread: EmailThread?,
                offset: Int,
                rejectedLabels: List<Label>): List<EmailThread> {
            val labels = db.labelDao().getAll()
            val selectedLabel = labels.findLast {label ->
                label.text == labelTextTypes
            }?.id
            val rejectedIdLabels = rejectedLabels.filter {label ->
                label.text != labelTextTypes
            }.map {
                it.id
            }
            val emails = if(oldestEmailThread != null)
                db.emailDao().getEmailThreadsFromMailboxLabel(
                        starterDate = oldestEmailThread.timestamp,
                        rejectedLabels = rejectedIdLabels,
                        selectedLabel = selectedLabel!!,
                        offset = offset )

            else
                db.emailDao().getInitialEmailThreadsFromMailboxLabel(
                        rejectedLabels = rejectedIdLabels,
                        selectedLabel = selectedLabel!!,
                        offset = offset )

            return emails.map { email ->
                getEmailThreadFromEmail(email)
            } as ArrayList<EmailThread>
        }

        override fun getLabelsFromLabelType(labelTextTypes: List<MailFolders>): List<Label> {
            val stringLabelTypes = labelTextTypes.map { labelTextType->
                LabelTextConverter().parseLabelTextType(labelTextType)
            }
            return db.labelDao().get(stringLabelTypes)
        }

        override fun getLabelFromLabelType(labelTextType: MailFolders): Label {
            val stringLabelType = LabelTextConverter().parseLabelTextType(labelTextType)

            return db.labelDao().get(labelTextType)
        }

        override fun deleteRelationByEmailIds(emailIds: List<Long>) {
            db.emailLabelDao().deleteRelationByEmailIds(emailIds)
        }

        override val getEmailThreadOperation = {
            threadId: String ->
            Result.of {
                val email = db.emailDao().getLatestEmailFromThreadId(threadId)
                getEmailThreadFromEmail(email)
            }
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
            return db.emailDao().getTotalUnreadThreads(emptyList(), labelId).size
        }

        override fun getTotalCounterLabel(labelId: Long): Int {
            return db.emailDao().getTotalThreads(labelId).size
        }
    }

}
