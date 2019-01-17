package com.criptext.mail.db

import com.criptext.mail.db.models.EmailLabel
import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.db.models.Label
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.EmailAddressUtils
import java.util.*

/**
 * Created by sebas on 3/12/18.
 */

interface EmailDetailLocalDB {

    fun getLabelsFromThreadId(threadId: String): List<Label>
    fun getFullEmailsFromThreadId(selectedLabel: String = "", threadId: String, rejectedLabels: List<Long>): List<FullEmail>
    fun unsendEmail(emailId: Long): Date
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

    class Default(private val db: AppDatabase): EmailDetailLocalDB {
        override fun setTrashDate(emailIds: List<Long>) {
            db.emailDao().updateEmailTrashDate(Date(), emailIds)
        }

        override fun unsendEmail(emailId: Long): Date {
            val date = Date()
            db.emailDao().changeDeliveryType(emailId, DeliveryTypes.UNSEND)
            db.emailDao().unsendEmailById(emailId, "", "",
                    date)
            db.fileDao().changeFileStatusByEmailid(emailId, 0)
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

                FullEmail(
                        email = it,
                        bcc = contactsBCC,
                        cc = contactsCC,
                        from = db.contactDao().getContact(
                                EmailAddressUtils.extractEmailAddress(it.fromAddress)
                        )?: contactsFROM[0],
                        files = files,
                        labels = labels,
                        to = contactsTO, fileKey = fileKey?.key)
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
            db.emailDao().deleteThreads(listOf(threadId))
        }

        override fun deleteRelationByLabelAndEmailIds(labelId: Long, emailIds: List<Long>){
            db.emailLabelDao().deleteRelationByLabelAndEmailIds(labelId, emailIds)
        }

        override fun deleteEmail(emailId: Long) {
            db.emailDao().deleteById(emailId)
        }

        override fun getCustomLabels(): List<Label>{
            return db.labelDao().getAllCustomLabels()
        }
    }


}
