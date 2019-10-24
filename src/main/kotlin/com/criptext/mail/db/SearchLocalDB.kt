package com.criptext.mail.db

import com.criptext.mail.db.models.*
import com.criptext.mail.scenes.mailbox.data.EmailThread
import com.criptext.mail.utils.ContactUtils
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.EmailThreadValidator
import com.criptext.mail.utils.EmailUtils
import java.io.File
import java.util.*

/**
 * Created by danieltigse on 2/5/18.
 */

interface SearchLocalDB{

    fun searchMailsInDB(
            userEmail: String,
            queryText: String,
            startDate: Date?,
            limit: Int, account: ActiveAccount): List<EmailThread>
    fun updateUnreadStatus(emailThreads: List<EmailThread>,
                           updateUnreadStatus: Boolean,
                           rejectedLabels: List<Long>, accountId: Long)

    class Default(private val db: AppDatabase, val filesDir: File): SearchLocalDB{

        override fun searchMailsInDB(userEmail: String, queryText: String,
                                     startDate: Date?,
                                     limit: Int, account: ActiveAccount): List<EmailThread> {

            val emails = if(startDate != null)
                db.emailDao().searchEmailThreads(
                        starterDate = startDate,
                        queryText = "%$queryText%",
                        rejectedLabels = listOf(Label.defaultItems.spam, Label.defaultItems.trash).map { it.id },
                        limit = limit , accountId = account.id)

            else
                db.emailDao().searchInitialEmailThreads(
                        queryText = "%$queryText%",
                        rejectedLabels = listOf(Label.defaultItems.spam, Label.defaultItems.trash).map { it.id },
                        limit = limit, accountId = account.id )

            return emails.map { email ->
                getEmailThreadFromEmail(email, Label.defaultItems.inbox.text,
                        Label.defaultItems.rejectedLabelsByMailbox(Label.defaultItems.inbox)
                                .map { it.id }, userEmail, account)
            } as ArrayList<EmailThread>

        }

        private fun getEmailThreadFromEmail(email: Email, selectedLabel: String,
                                            rejectedLabels: List<Long>, userEmail: String, account: ActiveAccount): EmailThread {

            val id = email.id
            val labels = db.emailLabelDao().getLabelsFromEmail(id)
            val contactsCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.CC)
            val contactsBCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.BCC)
            val contactsTO = db.emailContactDao().getContactsFromEmail(id, ContactTypes.TO)
            val files = db.fileDao().getAttachmentsFromEmail(id)
            val fileKey = db.fileKeyDao().getAttachmentKeyFromEmail(id)
            email.subject = email.subject.replace("^(Re|RE): ".toRegex(), "")
                    .replace("^(Fw|FW|Fwd|FWD): ".toRegex(), "")

            val emails = db.emailDao().getEmailsFromThreadId(email.threadId, rejectedLabels, account.id)
            var totalFiles = 0
            val headerData = mutableListOf<EmailThread.HeaderData>()
            val participants = emails.flatMap {
                val contacts = mutableListOf<Contact>()
                val emailLabels = db.emailLabelDao().getLabelsFromEmail(it.id)
                if(selectedLabel == Label.defaultItems.sent.text){
                    if(EmailThreadValidator.isLabelInList(emailLabels, Label.LABEL_SENT)){
                        contacts.addAll(db.emailContactDao().getContactsFromEmail(it.id, ContactTypes.TO))
                        contacts.addAll(db.emailContactDao().getContactsFromEmail(it.id, ContactTypes.CC))
                    }
                }
                else{
                    val fromContact = ContactUtils.getFromContact(db.emailContactDao(), db.contactDao(),
                            account.id, it.id, it.fromAddress)
                    contacts.addAll(listOf(fromContact))
                    contacts.addAll(db.emailContactDao().getContactsFromEmail(it.id, ContactTypes.FROM)
                            .filter { contact -> contact.id != fromContact.id })
                }
                contacts.map { contact ->
                    when {
                        EmailThreadValidator.isLabelInList(emailLabels, Label.LABEL_DRAFT) -> headerData.add(EmailThread.HeaderData(
                                name = EmailUtils.DRAFT_HEADER_PLACEHOLDER,
                                isDraft = true,
                                isMe = false,
                                isUnread = it.unread
                        ))
                        contact.email == userEmail -> headerData.add(EmailThread.HeaderData(
                                name = contact.name,
                                isDraft = false,
                                isMe = true,
                                isUnread = it.unread
                        ))
                        else -> headerData.add(EmailThread.HeaderData(
                                name = contact.name,
                                isDraft = false,
                                isMe = false,
                                isUnread = it.unread
                        ))
                    }
                }
                totalFiles += db.fileDao().getAttachmentsFromEmail(it.id).size
                contacts
            }


            val fromContact = ContactUtils.getFromContact(db.emailContactDao(), db.contactDao(),
                    account.id, email.id, email.fromAddress)

            val emailContent =  EmailUtils.getEmailContentFromFileSystem(filesDir,
                    email.metadataKey, email.content,
                    account.recipientId, account.domain)


            return EmailThread(
                    participants = participants.distinctBy { it.id },
                    currentLabel = selectedLabel,
                    latestEmail = FullEmail(
                            email = email,
                            bcc = contactsBCC,
                            cc = contactsCC,
                            from = fromContact,
                            files = files,
                            labels = labels,
                            to = contactsTO,
                            fileKey = fileKey?.key,
                            headers = emailContent.second),
                    totalEmails = emails.size,
                    hasFiles = totalFiles > 0,
                    allFilesAreInline = files.filter { it.cid != null && it.cid != "" }.size == totalFiles,
                    headerData = headerData.distinctBy { it.name }
            )
        }

        override fun updateUnreadStatus(emailThreads: List<EmailThread>,
                                        updateUnreadStatus: Boolean,
                                        rejectedLabels: List<Long>, accountId: Long) {
            emailThreads.forEach {
                val emailsIds = db.emailDao().getEmailsFromThreadId(it.threadId, rejectedLabels, accountId)
                        .map {
                            it.id
                        }
                db.emailDao().toggleRead(ids = emailsIds, unread = updateUnreadStatus, accountId = accountId)
            }
        }
    }

}