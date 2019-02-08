package com.criptext.mail.db

import com.criptext.mail.db.models.*
import com.criptext.mail.scenes.mailbox.data.EmailThread
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
            limit: Int): List<EmailThread>
    fun updateUnreadStatus(emailThreads: List<EmailThread>,
                           updateUnreadStatus: Boolean,
                           rejectedLabels: List<Long>)

    class Default(private val db: AppDatabase, val filesDir: File): SearchLocalDB{

        override fun searchMailsInDB(userEmail: String, queryText: String,
                                     startDate: Date?,
                                     limit: Int): List<EmailThread> {

            val emails = if(startDate != null)
                db.emailDao().searchEmailThreads(
                        starterDate = startDate,
                        queryText = "%$queryText%",
                        rejectedLabels = listOf(Label.defaultItems.spam, Label.defaultItems.trash).map { it.id },
                        limit = limit )

            else
                db.emailDao().searchInitialEmailThreads(
                        queryText = "%$queryText%",
                        rejectedLabels = listOf(Label.defaultItems.spam, Label.defaultItems.trash).map { it.id },
                        limit = limit )

            return emails.map { email ->
                getEmailThreadFromEmail(email, Label.defaultItems.inbox.text,
                        Label.defaultItems.rejectedLabelsByMailbox(Label.defaultItems.inbox)
                                .map { it.id }, userEmail)
            } as ArrayList<EmailThread>

        }

        private fun getEmailThreadFromEmail(email: Email, selectedLabel: String,
                                            rejectedLabels: List<Long>, userEmail: String): EmailThread {

            val id = email.id
            val labels = db.emailLabelDao().getLabelsFromEmail(id)
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
                if(selectedLabel == Label.defaultItems.sent.text){
                    val emailLabels = db.emailLabelDao().getLabelsFromEmail(it.id)
                    if(EmailThreadValidator.isLabelInList(emailLabels, Label.LABEL_SENT)){
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


            val fromContact = if(EmailAddressUtils.checkIfOnlyHasEmail(email.fromAddress)){
                contactsFROM[0]
            }else Contact(
                    id = 0,
                    email = EmailAddressUtils.extractEmailAddress(email.fromAddress),
                    name = EmailAddressUtils.extractName(email.fromAddress),
                    isTrusted = contactsFROM[0].isTrusted,
                    score = contactsFROM[0].score
            )

            val emailContent =  EmailUtils.getEmailContentFromFileSystem(filesDir,
                    email.metadataKey, email.content,
                    db.accountDao().getLoggedInAccount()!!.recipientId)


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
                    hasFiles = totalFiles > 0
            )
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
    }

}