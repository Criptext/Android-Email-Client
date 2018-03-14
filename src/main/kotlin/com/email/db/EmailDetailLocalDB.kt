package com.email.db

import android.content.Context
import com.email.db.models.FullEmail

/**
 * Created by sebas on 3/12/18.
 */

interface EmailDetailLocalDB {

    fun getFullEmailsFromThreadId(threadId: String): List<FullEmail>

    class Default(applicationContext: Context): EmailDetailLocalDB {

        private val db = AppDatabase.getAppDatabase(applicationContext)

        override fun getFullEmailsFromThreadId(threadId: String): List<FullEmail> {
            val emails = db.emailDao().getEmailsFromThreadId(threadId)
            val fullEmails =  emails.map {
                val id = it.id!!
                val labels = db.emailLabelDao().getLabelsFromEmail(id)
                val contactsCC = db.emailContactDao().getContactsFromEmailCC(id)
                val contactsBCC = db.emailContactDao().getContactsFromEmailBCC(id)
                val contactsFROM = db.emailContactDao().getContactsFromEmailFROM(id)
                val contactsTO = db.emailContactDao().getContactsFromEmailTO(id)
                val files = db.fileDao().getAttachmentsFromEmail(id)

                FullEmail(
                        email = it,
                        bcc = contactsBCC,
                        cc = contactsCC,
                        from = contactsFROM,
                        files = files,
                        labels = labels,
                        to = contactsTO )
            }
            fullEmails.last().viewOpen = true
            return fullEmails
        }
    }

}
