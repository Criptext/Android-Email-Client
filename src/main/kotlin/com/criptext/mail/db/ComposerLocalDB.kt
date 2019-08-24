package com.criptext.mail.db

import com.criptext.mail.db.dao.*
import com.criptext.mail.db.models.*
import com.criptext.mail.scenes.mailbox.data.EmailThread
import com.criptext.mail.utils.ContactUtils
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.EmailThreadValidator
import com.criptext.mail.utils.EmailUtils
import java.io.File

/**
 * Created by danieltigse on 4/17/18.
 */

class ComposerLocalDB(val contactDao: ContactDao, val emailDao: EmailDao, val fileDao: FileDao,
                      val fileKeyDao: FileKeyDao, val labelDao: LabelDao, val emailLabelDao: EmailLabelDao,
                      val emailContactDao: EmailContactJoinDao, val accountDao: AccountDao, val filesDir: File) {

    fun loadFullEmail(id: Long, account: ActiveAccount): FullEmail? {
        val email = emailDao.findEmailById(id, account.id) ?: return null
        val labels = emailLabelDao.getLabelsFromEmail(id)
        val contactsCC = emailContactDao.getContactsFromEmail(id, ContactTypes.CC)
        val contactsBCC = emailContactDao.getContactsFromEmail(id, ContactTypes.BCC)
        val contactsFROM = emailContactDao.getContactsFromEmail(id, ContactTypes.FROM)
        val contactsTO = emailContactDao.getContactsFromEmail(id, ContactTypes.TO)
        val files = fileDao.getAttachmentsFromEmail(id)
        val fileKey = fileKeyDao.getAttachmentKeyFromEmail(id)

        val fromContact = ContactUtils.getFromContact(emailContactDao, contactDao,
                account.id, email.id, email.fromAddress)

        val emailContent =  EmailUtils.getEmailContentFromFileSystem(filesDir,
                email.metadataKey, email.content,
                account.recipientId, account.domain)

        return FullEmail(
                email = email.copy(content = emailContent.first),
                bcc = contactsBCC,
                cc = contactsCC,
                from = fromContact,
                files = files,
                labels = labels,
                to = contactsTO,
                fileKey = fileKey?.key,
                headers = emailContent.second)
    }

    fun getLabelById(id: Long, accountId: Long): Label? {
        return labelDao.getById(id, accountId)
    }

    fun getEmailThreadFromEmail(email: Email, selectedLabel: String,
                                rejectedLabels: List<Long>, userEmail: String, activeAccount: ActiveAccount): EmailThread {

        return emailThread(email, rejectedLabels, selectedLabel, userEmail, activeAccount)
    }

    private fun emailThread(email: Email, rejectedLabels: List<Long>, selectedLabel: String,
                            userEmail: String, activeAccount: ActiveAccount): EmailThread {
        val id = email.id
        val labels = emailLabelDao.getLabelsFromEmail(id)
        val emailsInSelectedLabel = if(selectedLabel != Label.LABEL_ALL_MAIL)
            emailLabelDao.getEmailCountInLabelByEmailId(email.threadId,
                    labelDao.get(selectedLabel, activeAccount.id).id) else -1
        val contactsCC = emailContactDao.getContactsFromEmail(id, ContactTypes.CC)
        val contactsBCC = emailContactDao.getContactsFromEmail(id, ContactTypes.BCC)
        val contactsFROM = emailContactDao.getContactsFromEmail(id, ContactTypes.FROM)
        val contactsTO = emailContactDao.getContactsFromEmail(id, ContactTypes.TO)
        val files = fileDao.getAttachmentsFromEmail(id)
        val fileKey: FileKey? = fileKeyDao.getAttachmentKeyFromEmail(id)
        email.subject = email.subject.replace("^(Re|RE): ".toRegex(), "")
                .replace("^(Fw|FW|Fwd|FWD): ".toRegex(), "")

        val emails = emailDao.getEmailsFromThreadId(email.threadId, rejectedLabels, activeAccount.id)
        var totalFiles = 0
        val headerData = mutableListOf<EmailThread.HeaderData>()
        val participants = emails.flatMap {
            val contacts = mutableListOf<Contact>()
            val emailLabels = emailLabelDao.getLabelsFromEmail(it.id)
            if (selectedLabel == Label.defaultItems.sent.text) {
                if (EmailThreadValidator.isLabelInList(emailLabels, Label.LABEL_SENT)) {
                    contacts.addAll(emailContactDao.getContactsFromEmail(it.id, ContactTypes.TO))
                    contacts.addAll(emailContactDao.getContactsFromEmail(it.id, ContactTypes.CC))
                }
            } else {
                val fromContact = ContactUtils.getFromContact(emailContactDao, contactDao,
                        activeAccount.id, it.id, it.fromAddress)
                contacts.addAll(listOf(fromContact))
                contacts.addAll(emailContactDao.getContactsFromEmail(it.id, ContactTypes.FROM)
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
            totalFiles += fileDao.getAttachmentsFromEmail(it.id).size
            contacts
        }

        val fromContact = ContactUtils.getFromContact(emailContactDao, contactDao,
                activeAccount.id, email.id, email.fromAddress)
        val emailContent =  EmailUtils.getEmailContentFromFileSystem(filesDir,
                email.metadataKey, email.content,
                activeAccount.recipientId, activeAccount.domain)

        return EmailThread(
                participants = participants.distinctBy { it.id },
                currentLabel = selectedLabel,
                latestEmail = FullEmail(
                        email = email.copy(
                                content = emailContent.first),
                        bcc = contactsBCC,
                        cc = contactsCC,
                        from = fromContact,
                        files = files,
                        labels = labels,
                        to = contactsTO,
                        fileKey = fileKey?.key,
                        headers = emailContent.second),
                totalEmails = getEmailCount(emailsInSelectedLabel, emails.size, selectedLabel),
                hasFiles = totalFiles > 0,
                allFilesAreInline = files.filter { it.cid != null }.size == totalFiles,
                headerData = headerData.distinctBy { it.name }
        )
    }

    private fun getEmailCount(emailsInSelectedLabel: Int, emailsSize: Int,
                              selectedLabel: String): Int{
        return if(selectedLabel == Label.LABEL_TRASH || selectedLabel == Label.LABEL_SPAM )
            emailsInSelectedLabel
        else
            emailsSize
    }
}
