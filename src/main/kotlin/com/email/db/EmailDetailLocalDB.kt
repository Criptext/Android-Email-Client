package com.email.db

import android.content.Context
import com.email.db.models.EmailLabel
import com.email.db.models.FullEmail
import com.email.db.models.Label
import com.email.scenes.mailbox.data.EmailThread
import java.util.HashSet

/**
 * Created by sebas on 3/12/18.
 */

interface EmailDetailLocalDB {

    fun getLabelsFromThreadId(threadId: String): List<Label>
    fun getFullEmailsFromThreadId(threadId: String): List<FullEmail>
    fun unsendEmail(emailId: Long)
    fun deleteRelationByEmailIds(emailIds: List<Long>)
    fun getLabelFromLabelType(labelTextType: MailFolders): Label
    fun createLabelEmailRelations(emailLabels: List<EmailLabel>)
    fun updateUnreadStatus(emailIds: List<Long>, updateUnreadStatus: Boolean)

    class Default(applicationContext: Context): EmailDetailLocalDB {

        private val db = AppDatabase.getAppDatabase(applicationContext)

        override fun unsendEmail(emailId: Long) {
            db.emailDao().changeDeliveryType(emailId, DeliveryTypes.UNSENT)
        }

        override fun getFullEmailsFromThreadId(threadId: String): List<FullEmail> {
            val emails = db.emailDao().getEmailsFromThreadId(threadId)
            val fullEmails =  emails.map {
                val id = it.id
                val labels = db.emailLabelDao().getLabelsFromEmail(id)
                val contactsCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.CC)
                val contactsBCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.BCC)
                val contactsFROM = db.emailContactDao().getContactsFromEmail(id, ContactTypes.FROM)
                val contactsTO = db.emailContactDao().getContactsFromEmail(id, ContactTypes.TO)
                val files = db.fileDao().getAttachmentsFromEmail(id)

                FullEmail(
                        email = it,
                        bcc = contactsBCC,
                        cc = contactsCC,
                        from = contactsFROM[0],
                        files = files,
                        labels = labels,
                        to = contactsTO )
            }

            fullEmails.last().viewOpen = true
            return fullEmails
        }

        override fun getLabelsFromThreadId(threadId: String): List<Label> {
            return db.emailLabelDao().getLabelsFromEmailThreadId(threadId)
        }

        override fun deleteRelationByEmailIds(emailIds: List<Long>) {
            db.emailLabelDao().deleteRelationByEmailIds(emailIds)
        }

        override fun getLabelFromLabelType(labelTextType: MailFolders): Label {
            return db.labelDao().get(labelTextType)
        }

        override fun createLabelEmailRelations(emailLabels: List<EmailLabel>){
            return db.emailLabelDao().insertAll(emailLabels)
        }

        override fun updateUnreadStatus(emailIds: List<Long>, updateUnreadStatus: Boolean) {
            db.emailDao().toggleRead(ids = emailIds, unread = updateUnreadStatus)
        }
    }


}
