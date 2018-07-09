package com.email.db

import android.content.Context
import com.email.db.models.EmailLabel
import com.email.db.models.FullEmail
import com.email.db.models.Label

/**
 * Created by sebas on 3/12/18.
 */

interface EmailDetailLocalDB {

    fun getLabelsFromThreadId(threadId: String): List<Label>
    fun getFullEmailsFromThreadId(threadId: String, rejectedLabels: List<Long>): List<FullEmail>
    fun unsendEmail(emailId: Long)
    fun deleteRelationByEmailIds(emailIds: List<Long>)
    fun getLabelByName(labelName: String): Label
    fun createLabelEmailRelations(emailLabels: List<EmailLabel>)
    fun updateUnreadStatus(emailIds: List<Long>, updateUnreadStatus: Boolean)
    fun deleteThread(threadId: String)
    fun deleteEmail(emailId: Long)
    fun deleteRelationByLabelAndEmailIds(labelId: Long, emailIds: List<Long>)
    fun getCustomLabels(): List<Label>

    class Default(applicationContext: Context): EmailDetailLocalDB {

        private val db = AppDatabase.getAppDatabase(applicationContext)

        override fun unsendEmail(emailId: Long) {
            db.emailDao().changeDeliveryType(emailId, DeliveryTypes.UNSEND)
        }

        override fun getFullEmailsFromThreadId(threadId: String, rejectedLabels: List<Long>): List<FullEmail> {
            val emails = db.emailDao().getEmailsFromThreadId(threadId, rejectedLabels)
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
