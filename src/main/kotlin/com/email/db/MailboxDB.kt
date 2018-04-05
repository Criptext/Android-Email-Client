package com.email.db

import android.content.Context
import com.email.db.models.*
import com.email.db.typeConverters.LabelTextConverter
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.labelChooser.data.LabelWrapper

/**
 * Created by sebas on 1/26/18.
 */

interface MailboxLocalDB {
    fun getAllEmailThreads(): List<EmailThread>
    fun getArchivedEmailThreads(): List<EmailThread>
    fun getAllLabelWrappers(): List<LabelWrapper>
    fun getAllLabels(): List<Label>
    fun getNotArchivedEmailThreads(): List<EmailThread>
    fun removeLabelsRelation(labels: List<Label>, emailId: Int)
    fun seed()
    fun deleteEmailThreads(emailThreads: List<EmailThread>)
    fun createLabelEmailRelation(labelId: Int, emailId: Int)
    fun updateUnreadStatus(emailThreads: List<EmailThread>,
                           updateUnreadStatus: Boolean)
    fun moveSelectedEmailThreadsToSpam(emailThreads: List<EmailThread>)
    fun moveSelectedEmailThreadsToTrash(emailThreads: List<EmailThread>)
    fun getLabelsFromThreadIds(threadIds: List<String>): List<Label>
    fun addEmail(email: Email) : Int
    fun createLabelsForEmailInbox(insertedEmailId: Int)
    fun createContacts(contacts: String, insertedEmailId: Int, type: ContactTypes)
    fun getEmailsFromMailboxLabel(
            labelTextType: LabelTextTypes,
            oldestEmailThread: EmailThread?,
            offset: Int,
            rejectedLabels: List<Label>): List<EmailThread>

     fun getLabelsFromLabelType(labelTextTypes: List<LabelTextTypes>): List<Label>

    fun deleteRelationByEmailIds(emailIds: List<Int>)
    fun getLabelFromLabelType(labelTextType: LabelTextTypes): Label

    class Default(private val db: AppDatabase): MailboxLocalDB {
        override fun createLabelsForEmailInbox(insertedEmailId: Int) {
            val labelInbox = db.labelDao().get(LabelTextTypes.INBOX)
            db.emailLabelDao().insert(EmailLabel(
                    labelId = labelInbox.id!!,
                    emailId = insertedEmailId))
        }

        private fun insertContact(contactEmail: String, emailId: Int, type: ContactTypes) {
            if(contactEmail.isNotEmpty()) {
                val contact = Contact(email = contactEmail, name = contactEmail)
                val emailContact = EmailContact(
                        contactId = contactEmail,
                        emailId = emailId,
                        type = type)
                db.contactDao().insert(contact)
                db.emailContactDao().insert(emailContact)
            }
        }

        override fun addEmail(email: Email): Int {
            db.emailDao().insert(email)
            return db.emailDao().getLastInsertedId()
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

        override fun createLabelEmailRelation(labelId: Int, emailId: Int) {
            val emailLabel = EmailLabel(labelId = labelId, emailId = emailId)
            return db.emailLabelDao().insert(emailLabel)
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

        override fun removeLabelsRelation(labels: List<Label>, emailId: Int) {
            labels.forEach{
                db.emailLabelDao().deleteByEmailLabelIds(it.id!!, emailId)
            }
        }

        override fun seed() {
            try {
            } catch (e: Exception) {
                e.printStackTrace()
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
                db.emailDao().toggleRead(id = it.latestEmail.email.id!!,
                        unread = updateUnreadStatus)
            }
        }

        override fun moveSelectedEmailThreadsToSpam(emailThreads: List<EmailThread>) {
            TODO("MOVE EMAILS TO SPAM")
        }

        override fun moveSelectedEmailThreadsToTrash(emailThreads: List<EmailThread>) {
            val emails = emailThreads.map {
                val trashLabel = db.labelDao().get(LabelTextTypes.TRASH)
                db.emailLabelDao().insert(
                        EmailLabel(
                                emailId = it.id,
                                labelId = trashLabel.id!!))

                it.latestEmail.email.isTrash = true
                    it.latestEmail.email
                }

            db.emailDao().update(emails)
        }

        private fun getEmailThreadFromEmail(email: Email): EmailThread {
            val id = email.id!!
            val labels = db.emailLabelDao().getLabelsFromEmail(id)
            val contactsCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.CC)
            val contactsBCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.BCC)
            val contactsFROM = db.emailContactDao().getContactsFromEmail(id, ContactTypes.FROM)
            val contactsTO = db.emailContactDao().getContactsFromEmail(id, ContactTypes.TO)
            val files = db.fileDao().getAttachmentsFromEmail(id)

            val contactFrom = if(contactsFROM.isEmpty()) {
                null
            } else {
                contactsFROM[0]
            }

            return EmailThread(
                    latestEmail = FullEmail(
                            email = email,
                            bcc = contactsBCC,
                            cc = contactsCC,
                            from = contactFrom,
                            files = files,
                            labels = labels,
                            to = contactsTO ),
                    labelsOfMail = db.emailLabelDao().getLabelsFromEmail(email.id!!) as ArrayList<Label>
            )

        }

        override fun createContacts(contacts: String, insertedEmailId: Int, type: ContactTypes) {
            if(type == ContactTypes.FROM) {
                insertContact(
                        contactEmail = contacts,
                        emailId = insertedEmailId,
                        type = type)
               return
            }

            val contactsList = contacts.split(",")
            contactsList.forEach { contactEmail ->
                insertContact(
                        contactEmail = contactEmail,
                        emailId = insertedEmailId,
                        type = type)
            }
        }

        override fun getEmailsFromMailboxLabel(
                labelTextTypes: LabelTextTypes,
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
                it.id!!
            }
            val emails: List<Email>
            if(oldestEmailThread != null) {
                emails =  db.emailDao().getEmailThreadsFromMailboxLabel(
                        starterDate = oldestEmailThread.timestamp,
                        rejectedLabels = rejectedIdLabels,
                        selectedLabel = selectedLabel!!,
                        offset = offset )

            } else {
                emails =  db.emailDao().getInitialEmailThreadsFromMailboxLabel(
                        rejectedLabels = rejectedIdLabels,
                        selectedLabel = selectedLabel!!,
                        offset = offset )
            }
            return emails.map { email ->
                getEmailThreadFromEmail(email)
            } as ArrayList<EmailThread>
        }

        override fun getLabelsFromLabelType(labelTextTypes: List<LabelTextTypes>): List<Label> {
            val stringLabelTypes = labelTextTypes.map { labelTextType->
                LabelTextConverter().parseLabelTextType(labelTextType)
            }
            return db.labelDao().get(stringLabelTypes)
        }

        override fun getLabelFromLabelType(labelTextType: LabelTextTypes): Label {
            val stringLabelType = LabelTextConverter().parseLabelTextType(labelTextType)

            return db.labelDao().get(labelTextType)
        }

        override fun deleteRelationByEmailIds(emailIds: List<Int>) {
            db.emailLabelDao().deleteRelationByEmailIds(emailIds)
        }
    }
}
