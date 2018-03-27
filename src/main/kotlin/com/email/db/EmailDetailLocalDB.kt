package com.email.db

import android.content.Context
import com.email.db.models.FullEmail

/**
 * Created by sebas on 3/12/18.
 */

interface EmailDetailLocalDB {

    fun getFullEmailsFromThreadId(threadId: String): List<FullEmail>
    fun unsendEmail(emailId: Int)

    class Default(applicationContext: Context): EmailDetailLocalDB {

        private val db = AppDatabase.getAppDatabase(applicationContext)

        override fun unsendEmail(emailId: Int) {
            db.emailDao().changeDeliveryType(emailId, DeliveryTypes.UNSENT)
        }

        override fun getFullEmailsFromThreadId(threadId: String): List<FullEmail> {
            val emails = db.emailDao().getEmailsFromThreadId(threadId)
            val fullEmails =  emails.map {
                val id = it.id!!
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

                FullEmail(
                        email = it,
                        bcc = contactsBCC,
                        cc = contactsCC,
                        from = contactFrom,
                        files = files,
                        labels = labels,
                        to = contactsTO )
            }

            fullEmails.last().viewOpen = true
            return fullEmails
        }
    }


}
