package com.email.activities.mailbox.data

import com.email.DB.AppDatabase
import com.email.DB.models.Email
import com.email.DB.models.Label

/**
 * Created by sebas on 1/24/18.
 */

class MailboxDataSource(private val db: AppDatabase) {

    fun getMailbox(): List<Email> {
        return db.emailDao().getAll()
    }

    fun getEmailThreads(): ArrayList<EmailThread> {
        val mails: List<Email> = this.getMailbox()
        val emailThreads: ArrayList<EmailThread> = mails.map { email ->
            EmailThread(latestMail = email,
                    labelsOfMail = db.emailLabelDao().getLabelsFromEmail(email.id) as ArrayList<Label>,
                    count = -1,
                    hasEmailAttachments = false)
        } as ArrayList<EmailThread>

        return emailThreads
    }
}
