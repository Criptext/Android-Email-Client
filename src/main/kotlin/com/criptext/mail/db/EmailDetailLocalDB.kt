package com.criptext.mail.db

import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.EmailLabel
import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.db.models.Label
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
    fun getFullEmailsFromThreadId(selectedLabel: String = "", threadId: String, rejectedLabels: List<Long>): List<FullEmail>
    fun unsendEmail(emailId: Long): Date
    fun getEmailMetadataKeyById(emailId: Long): Long
    fun deleteRelationByEmailIds(emailIds: List<Long>)
    fun getLabelByName(labelName: String): Label
    fun getLabelsByName(labelName: List<String>): List<Label>
    fun createLabelEmailRelations(emailLabels: List<EmailLabel>)
    fun updateUnreadStatus(emailIds: List<Long>, updateUnreadStatus: Boolean)
    fun deleteThread(threadId: String)
    fun deleteEmail(emailId: Long)
    fun deleteRelationByLabelAndEmailIds(labelId: Long, emailIds: List<Long>)
    fun getCustomLabels(): List<Label>
    fun setTrashDate(emailIds: List<Long>)
    fun getInternalFilesDir(): String

    class Default(private val db: AppDatabase, private val filesDir: File): EmailDetailLocalDB {
        override fun getEmailMetadataKeyById(emailId: Long): Long {
            return db.emailDao().getEmailById(emailId)!!.metadataKey
        }

        override fun getInternalFilesDir(): String {
            return filesDir.path
        }

        override fun setTrashDate(emailIds: List<Long>) {
            db.emailDao().updateEmailTrashDate(Date(), emailIds)
        }

        override fun unsendEmail(emailId: Long): Date {
            val date = Date()
            val metadataKey = db.emailDao().findEmailById(emailId)!!.metadataKey
            db.emailDao().changeDeliveryType(emailId, DeliveryTypes.UNSEND)
            db.emailDao().unsendEmailById(emailId, "", "",
                    date)
            db.fileDao().changeFileStatusByEmailid(emailId, 0)
            EmailUtils.deleteEmailInFileSystem(
                    filesDir = filesDir,
                    recipientId = db.accountDao().getLoggedInAccount()!!.recipientId,
                    metadataKey = metadataKey
            )
            return date
        }

        override fun getFullEmailsFromThreadId(selectedLabel: String,
                                               threadId: String, rejectedLabels: List<Long>): List<FullEmail> {
            val emails = if(selectedLabel == Label.LABEL_SPAM || selectedLabel == Label.LABEL_TRASH)
                db.emailDao().getEmailsFromThreadIdByLabel(db.labelDao().get(selectedLabel).id, listOf(threadId))
            else
                db.emailDao().getEmailsFromThreadId(threadId, rejectedLabels)
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
                        db.accountDao().getLoggedInAccount()!!.recipientId)

                FullEmail(
                        email = it.copy(
                                content = emailContent.first
                        ),
                        bcc = contactsBCC,
                        cc = contactsCC,
                        from = if(EmailAddressUtils.checkIfOnlyHasEmail(it.fromAddress)){
                            contactsFROM[0]
                        }else Contact(
                                id = 0,
                                email = EmailAddressUtils.extractEmailAddress(it.fromAddress),
                                name = EmailAddressUtils.extractName(it.fromAddress),
                                isTrusted = contactsFROM[0].isTrusted
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

        override fun getLabelByName(labelName: String): Label {
            return db.labelDao().get(labelName)
        }

        override fun getLabelsByName(labelName: List<String>): List<Label> {
            return db.labelDao().get(labelName)
        }

        override fun createLabelEmailRelations(emailLabels: List<EmailLabel>){
            return db.emailLabelDao().insertAll(emailLabels)
        }

        override fun updateUnreadStatus(emailIds: List<Long>, updateUnreadStatus: Boolean) {
            db.emailDao().toggleRead(ids = emailIds, unread = updateUnreadStatus)
        }

        override fun deleteThread(threadId: String) {
            db.emailDao().getAllEmailsByThreadId(threadId).forEach {
                EmailUtils.deleteEmailInFileSystem(
                        filesDir = filesDir,
                        metadataKey = it.metadataKey,
                        recipientId = db.accountDao().getLoggedInAccount()!!.recipientId)
            }
            db.emailDao().deleteThreads(listOf(threadId))
        }

        override fun deleteRelationByLabelAndEmailIds(labelId: Long, emailIds: List<Long>){
            db.emailLabelDao().deleteRelationByLabelAndEmailIds(labelId, emailIds)
        }

        override fun deleteEmail(emailId: Long) {
            val email = db.emailDao().findEmailById(emailId)
            EmailUtils.deleteEmailInFileSystem(
                    filesDir = filesDir,
                    recipientId = db.accountDao().getLoggedInAccount()!!.recipientId,
                    metadataKey = email!!.metadataKey
            )
            db.emailDao().deleteById(emailId)
        }

        override fun getCustomLabels(): List<Label>{
            return db.labelDao().getAllCustomLabels()
        }
    }


}
