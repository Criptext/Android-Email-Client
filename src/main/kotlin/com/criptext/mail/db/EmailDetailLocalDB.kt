package com.criptext.mail.db

import com.criptext.mail.db.models.*
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.EmailUtils
import java.io.File
import java.util.*

/**
 * Created by sebas on 3/12/18.
 */

interface EmailDetailLocalDB {

    fun getLabelsFromThreadId(threadId: String): List<Label>
    fun getFullEmailsFromThreadId(selectedLabel: String = "", threadId: String, rejectedLabels: List<Long>, account: ActiveAccount): List<FullEmail>
    fun unsendEmail(emailId: Long, account: ActiveAccount): Date
    fun getEmailMetadataKeyById(emailId: Long, accountId: Long): Long
    fun deleteRelationByEmailIds(emailIds: List<Long>)
    fun getLabelByName(labelName: String, accountId: Long): Label
    fun getLabelsByName(labelName: List<String>, accountId: Long): List<Label>
    fun getLabelsById(ids: List<Long>, accountId: Long): List<Label>
    fun getLabelById(id: Long, accountId: Long): Label?
    fun createLabelEmailRelations(emailLabels: List<EmailLabel>)
    fun updateUnreadStatus(emailIds: List<Long>, updateUnreadStatus: Boolean, accountId: Long)
    fun deleteThread(threadId: String, account: ActiveAccount)
    fun deleteEmail(emailId: Long, account: ActiveAccount)
    fun deleteRelationByLabelAndEmailIds(labelId: Long, emailIds: List<Long>, accountId: Long)
    fun getCustomLabels(accountId: Long): List<Label>
    fun setTrashDate(emailIds: List<Long>, accountId: Long)
    fun getInternalFilesDir(): String
    fun updateSpamCounter(emailIds: List<Long>, accountId: Long, userEmail: String): List<String>
    fun resetSpamCounter(emailIds: List<Long>, accountId: Long, userEmail: String): List<String>

    class Default(private val db: AppDatabase, private val filesDir: File): EmailDetailLocalDB {
        override fun resetSpamCounter(emailIds: List<Long>, accountId: Long, userEmail: String): List<String> {
            val emails = db.emailDao().getAllEmailsbyId(emailIds, accountId)
            val fromContacts = emails.filter { !it.fromAddress.contains(userEmail) }.map { EmailAddressUtils.extractEmailAddress(it.fromAddress) }
            db.contactDao().resetSpamCounter(fromContacts, accountId)
            return fromContacts
        }

        override fun updateSpamCounter(emailIds: List<Long>, accountId: Long, userEmail: String): List<String> {
            val emails = db.emailDao().getAllEmailsbyId(emailIds, accountId)
            val fromContacts = emails.filter { !it.fromAddress.contains(userEmail) }.map { EmailAddressUtils.extractEmailAddress(it.fromAddress) }
            db.contactDao().uptickSpamCounter(fromContacts, accountId)
            return fromContacts
        }

        override fun getEmailMetadataKeyById(emailId: Long, accountId: Long): Long {
            return db.emailDao().getEmailById(emailId, accountId)!!.metadataKey
        }

        override fun getInternalFilesDir(): String {
            return filesDir.path
        }

        override fun setTrashDate(emailIds: List<Long>, accountId: Long) {
            db.emailDao().updateEmailTrashDate(Date(), emailIds, accountId)
        }

        override fun unsendEmail(emailId: Long, account: ActiveAccount): Date {
            val date = Date()
            val metadataKey = db.emailDao().findEmailById(emailId, account.id)!!.metadataKey
            db.emailDao().changeDeliveryType(emailId, DeliveryTypes.UNSEND, account.id)
            db.emailDao().unsendEmailById(emailId, "", "",
                    date, account.id)
            db.fileDao().changeFileStatusByEmailid(emailId, 0)
            EmailUtils.deleteEmailInFileSystem(
                    filesDir = filesDir,
                    recipientId = account.recipientId,
                    domain = account.domain,
                    metadataKey = metadataKey
            )
            return date
        }

        override fun getFullEmailsFromThreadId(selectedLabel: String,
                                               threadId: String, rejectedLabels: List<Long>, account: ActiveAccount): List<FullEmail> {
            val emails = if(selectedLabel == Label.LABEL_SPAM || selectedLabel == Label.LABEL_TRASH)
                db.emailDao().getEmailsFromThreadIdByLabel(db.labelDao().get(selectedLabel, account.id).id, listOf(threadId), account.id)
            else
                db.emailDao().getEmailsFromThreadId(threadId, rejectedLabels, account.id)
            val fullEmails =  emails.map {
                val id = it.id
                val labels = db.emailLabelDao().getLabelsFromEmail(id)
                val contactsCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.CC)
                val contactsBCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.BCC)
                val contactsFROM = db.emailContactDao().getContactsFromEmail(id, ContactTypes.FROM)
                val contactsTO = db.emailContactDao().getContactsFromEmail(id, ContactTypes.TO)
                val files = db.fileDao().getAttachmentsFromEmail(id)
                val fileKey = db.fileKeyDao().getAttachmentKeyFromEmail(id)

                val emailContent = EmailUtils.getEmailContentFromFileSystem(filesDir,
                        it.metadataKey, it.content,
                        account.recipientId, account.domain)

                FullEmail(
                        email = it.copy(
                                content = emailContent.first
                        ),
                        bcc = contactsBCC,
                        cc = contactsCC,
                        from = if(it.fromAddress.isEmpty()){
                            contactsFROM[0]
                        }else Contact(
                                id = contactsFROM[0].id,
                                email = EmailAddressUtils.extractEmailAddress(it.fromAddress),
                                name = EmailAddressUtils.extractName(it.fromAddress),
                                isTrusted = contactsFROM[0].isTrusted,
                                score = contactsFROM[0].score,
                                spamScore = contactsFROM[0].spamScore
                        ),
                        files = files,
                        labels = labels,
                        to = contactsTO, fileKey = fileKey?.key, headers = emailContent.second)
            }

            fullEmails.lastOrNull()?.viewOpen = true

            return fullEmails
        }

        override fun getLabelsFromThreadId(threadId: String): List<Label> {
            return db.emailLabelDao().getLabelsFromEmailThreadId(threadId)
        }

        override fun deleteRelationByEmailIds(emailIds: List<Long>) {
            db.emailLabelDao().deleteRelationByEmailIds(emailIds)
        }

        override fun getLabelByName(labelName: String, accountId: Long): Label {
            return db.labelDao().get(labelName, accountId)
        }

        override fun getLabelsByName(labelName: List<String>, accountId: Long): List<Label> {
            return db.labelDao().get(labelName, accountId)
        }

        override fun getLabelById(id: Long, accountId: Long): Label? {
            return db.labelDao().getLabelById(id, accountId)
        }

        override fun getLabelsById(ids: List<Long>, accountId: Long): List<Label> {
            return db.labelDao().getById(ids, accountId)
        }

        override fun createLabelEmailRelations(emailLabels: List<EmailLabel>){
            return db.emailLabelDao().insertAll(emailLabels)
        }

        override fun updateUnreadStatus(emailIds: List<Long>, updateUnreadStatus: Boolean, accountId: Long) {
            db.emailDao().toggleRead(ids = emailIds, unread = updateUnreadStatus, accountId = accountId)
        }

        override fun deleteThread(threadId: String, account: ActiveAccount) {
            db.emailDao().getAllEmailsByThreadId(threadId).forEach {
                EmailUtils.deleteEmailInFileSystem(
                        filesDir = filesDir,
                        metadataKey = it.metadataKey,
                        recipientId = account.recipientId,
                        domain = account.domain)
            }
            db.emailDao().deleteThreads(listOf(threadId), account.id)
        }

        override fun deleteRelationByLabelAndEmailIds(labelId: Long, emailIds: List<Long>, accountId: Long){
            db.emailLabelDao().deleteRelationByLabelAndEmailIds(labelId, emailIds)
        }

        override fun deleteEmail(emailId: Long, account: ActiveAccount) {
            val email = db.emailDao().findEmailById(emailId, account.id)
            EmailUtils.deleteEmailInFileSystem(
                    filesDir = filesDir,
                    recipientId = account.recipientId,
                    domain = account.domain,
                    metadataKey = email!!.metadataKey
            )
            db.emailDao().deleteById(emailId, account.id)
        }

        override fun getCustomLabels(accountId: Long): List<Label>{
            return db.labelDao().getAllCustomLabels(accountId)
        }
    }


}
