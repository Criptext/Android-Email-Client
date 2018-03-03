package com.email.db

import android.content.Context
import com.email.db.models.Email
import com.email.db.models.EmailLabel
import com.email.db.models.Label
import com.email.db.seeders.*
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.LabelChooser.data.LabelThread

/**
 * Created by sebas on 1/26/18.
 */

interface MailboxLocalDB {
    fun getAllEmailThreads(): List<EmailThread>
    fun getArchivedEmailThreads(): List<EmailThread>
    fun getAllLabels(): List<LabelThread>
    fun getNotArchivedEmailThreads(): List<EmailThread>
    fun removeLabelsRelation(labels: List<Label>, emailId: Int)
    fun seed()
    fun deleteEmailThreads(emailThreads: List<EmailThread>)
    fun createLabelEmailRelation(labelId: Int, emailId: Int)
    fun updateUnreadStatus(emailThreads: List<EmailThread>,
                           updateUnreadStatus: Boolean)
    fun moveSelectedEmailThreadsToSpam(emailThreads: List<EmailThread>)
    fun moveSelectedEmailThreadsToTrash(emailThreads: List<EmailThread>)


    class Default(applicationContext: Context): MailboxLocalDB {

        override fun createLabelEmailRelation(labelId: Int, emailId: Int) {
            val emailLabel = EmailLabel(labelId = labelId, emailId = emailId)
            return db.emailLabelDao().insert(emailLabel)
        }

        private val db = AppDatabase.getAppDatabase(applicationContext)

        override fun getAllEmailThreads(): List<EmailThread> {
            return db.emailDao().getAll().map { email ->
                EmailThread(email = email,
                        labelsOfMail = db.emailLabelDao().getLabelsFromEmail(email.id) as ArrayList<Label>)
            } as ArrayList<EmailThread>
        }

        override fun getArchivedEmailThreads(): List<EmailThread> {
            return db.emailDao().getAll().map { email ->
                EmailThread(email = email,
                        labelsOfMail = ArrayList())
            } as ArrayList<EmailThread>
        }

        override fun getNotArchivedEmailThreads(): List<EmailThread> {
            return db.emailDao().getNotArchivedEmailThreads().map { email ->
                EmailThread(email = email,
                        labelsOfMail = db.emailLabelDao().getLabelsFromEmail(email.id) as ArrayList<Label>)
            }
        }

        override fun removeLabelsRelation(labels: List<Label>, emailId: Int) {
            labels.forEach{
                db.emailLabelDao().deleteByEmailLabelIds(it.id, emailId)
            }
        }

        override fun seed() {
            try {
                LabelSeeder.seed(db.labelDao())
                EmailSeeder.seed(db.emailDao())
                EmailLabelSeeder.seed(db.emailLabelDao())
                FileSeeder.seed(db.fileDao())
                OpenSeeder.seed(db.openDao())
                EmailContactSeeder.seed(db.emailContactDao())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun deleteEmailThreads(emailThreads: List<EmailThread>) {
            val emails: List<Email> = emailThreads.map {
                it.email
            }
            db.emailDao().deleteAll(emails)
        }

        override fun getAllLabels(): List<LabelThread> {
            return db.labelDao().getAll().map{ label ->
                LabelThread(label)
            } as ArrayList<LabelThread>
        }

        override fun updateUnreadStatus(emailThreads: List<EmailThread>, updateUnreadStatus: Boolean) {
            emailThreads.forEach {
                db.emailDao().toggleRead(id = it.email.id,
                        unread = updateUnreadStatus)
            }
        }

        override fun moveSelectedEmailThreadsToSpam(emailThreads: List<EmailThread>) {
            TODO("MOVE EMAILS TO SPAM")
        }

        override fun moveSelectedEmailThreadsToTrash(emailThreads: List<EmailThread>) {
            val emails = emailThreads.map {
                    it.email.isTrash = true
                    it.email
                }

            db.emailDao().update(emails)
        }
    }

}
