package com.email.db

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.email.db.models.Email
import com.email.db.models.FullEmail
import com.email.db.models.Label
import com.email.scenes.mailbox.data.EmailThread
import java.util.ArrayList

/**
 * Created by danieltigse on 2/5/18.
 */

interface SearchLocalDB{

    fun searchMailsInDB(
            queryText: String,
            oldestEmailThread: EmailThread?,
            limit: Int): List<EmailThread>
    fun updateUnreadStatus(emailThreads: List<EmailThread>,
                           updateUnreadStatus: Boolean,
                           rejectedLabels: List<Long>)

    class Default(private val db: AppDatabase): SearchLocalDB{

        override fun searchMailsInDB(queryText: String,
                                     oldestEmailThread: EmailThread?,
                                     limit: Int): List<EmailThread> {

            val emails = if(oldestEmailThread != null)
                db.emailDao().searchEmailThreads(
                        starterDate = oldestEmailThread.timestamp,
                        queryText = "%$queryText%",
                        rejectedLabels = listOf(Label.defaultItems.spam, Label.defaultItems.trash).map { it.id },
                        limit = limit )

            else
                db.emailDao().searchInitialEmailThreads(
                        queryText = "%$queryText%",
                        rejectedLabels = listOf(Label.defaultItems.spam, Label.defaultItems.trash).map { it.id },
                        limit = limit )

            return emails.map { email ->
                getEmailThreadFromEmail(email)
            } as ArrayList<EmailThread>

        }

        private fun getEmailThreadFromEmail(email: Email): EmailThread {
            val id = email.id
            val labels = db.emailLabelDao().getLabelsFromEmail(id)
            val contactsCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.CC)
            val contactsBCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.BCC)
            val contactsFROM = db.emailContactDao().getContactsFromEmail(id, ContactTypes.FROM)
            val contactsTO = db.emailContactDao().getContactsFromEmail(id, ContactTypes.TO)
            val files = db.fileDao().getAttachmentsFromEmail(id)
            val totalEmails = db.emailDao().getTotalEmailsByThread(email.threadId, listOf())
            email.subject = email.subject.replace("^(Re|RE): ".toRegex(), "")
                    .replace("^(Fw|FW): ".toRegex(), "")

            return EmailThread(
                    latestEmail = FullEmail(
                            email = email,
                            bcc = contactsBCC,
                            cc = contactsCC,
                            from = contactsFROM[0],
                            files = files,
                            labels = labels,
                            to = contactsTO ),
                    totalEmails = totalEmails
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