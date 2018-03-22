package com.email.db

import android.content.Context
import com.email.db.models.Email
import com.email.db.models.EmailLabel
import com.email.db.models.Label
import com.email.db.seeders.*
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.labelChooser.data.LabelWrapper

/**
 * Created by sebas on 1/26/18.
 */

interface MailboxLocalDB {
    fun getAllEmailThreads(): List<EmailThread>
    fun getArchivedEmailThreads(): List<EmailThread>
    fun getAllLabelWrappers(): List<LabelWrapper>
    fun getAllLabels(): List<Label>
    fun getNotArchivedEmailThreads(): List<EmailThread>
    fun removeLabelsRelation(labels: List<Label>, emailId: Int)
    fun seed()
    fun deleteEmailThreads(emailThreads: List<EmailThread>)
    fun createLabelEmailRelation(labelId: Int, emailId: Int)
    fun updateUnreadStatus(emailThreads: List<EmailThread>,
                           updateUnreadStatus: Boolean)
    fun moveSelectedEmailThreadsToSpam(emailThreads: List<EmailThread>)
    fun moveSelectedEmailThreadsToTrash(emailThreads: List<EmailThread>)
    fun getLabelsFromThreadIds(threadIds: List<String>): List<Label>


    class Default(applicationContext: Context): MailboxLocalDB {

        override fun getLabelsFromThreadIds(threadIds: List<String>) : List<Label> {
            val labelSet = HashSet<Label>()

            threadIds.forEach {
                    val labels = db.
                            emailLabelDao().
                            getLabelsFromEmailThreadId(it)
                    labelSet.addAll(labels)
            }

            return labelSet.toList()
        }

        override fun createLabelEmailRelation(labelId: Int, emailId: Int) {
            val emailLabel = EmailLabel(labelId = labelId, emailId = emailId)
            return db.emailLabelDao().insert(emailLabel)
        }

        private val db = AppDatabase.getAppDatabase(applicationContext)

        override fun getAllEmailThreads(): List<EmailThread> {
            return db.emailDao().getAll().map { email ->
                EmailThread(
                        latestEmail = email,
                        labelsOfMail = db.emailLabelDao().getLabelsFromEmail(email.id!!) as ArrayList<Label>
                )
            } as ArrayList<EmailThread>
        }

        override fun getArchivedEmailThreads(): List<EmailThread> {
            return db.emailDao().getAll().map { email ->
                EmailThread(latestEmail = email,
                        labelsOfMail = ArrayList())
            } as ArrayList<EmailThread>
        }

        override fun getNotArchivedEmailThreads(): List<EmailThread> {
            return db.emailDao().getNotArchivedEmailThreads().map { email ->
                EmailThread(latestEmail = email,
                        labelsOfMail = db.emailLabelDao().getLabelsFromEmail(email.id!!) as ArrayList<Label>)
            }
        }

        override fun removeLabelsRelation(labels: List<Label>, emailId: Int) {
            labels.forEach{
                db.emailLabelDao().deleteByEmailLabelIds(it.id!!, emailId)
            }
        }

        override fun seed() {
            try {
                LabelSeeder.seed(db.labelDao())
                EmailSeeder.seed(db.emailDao())
                EmailLabelSeeder.seed(db.emailLabelDao())
                ContactSeeder.seed(db.contactDao())
                FileSeeder.seed(db.fileDao())
                OpenSeeder.seed(db.openDao())
                EmailContactSeeder.seed(db.emailContactDao())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun deleteEmailThreads(emailThreads: List<EmailThread>) {
            val emails: List<Email> = emailThreads.map {
                it.latestEmail
            }
            db.emailDao().deleteAll(emails)
        }

        override fun getAllLabelWrappers(): List<LabelWrapper> {
            return db.labelDao().getAll().map{ label ->
                LabelWrapper(label)
            } as ArrayList<LabelWrapper>
        }

        override fun getAllLabels(): List<Label> {
            return db.labelDao().getAll()
        }

        override fun updateUnreadStatus(emailThreads: List<EmailThread>, updateUnreadStatus: Boolean) {
            emailThreads.forEach {
                db.emailDao().toggleRead(id = it.latestEmail.id!!,
                        unread = updateUnreadStatus)
            }
        }

        override fun moveSelectedEmailThreadsToSpam(emailThreads: List<EmailThread>) {
            TODO("MOVE EMAILS TO SPAM")
        }

        override fun moveSelectedEmailThreadsToTrash(emailThreads: List<EmailThread>) {
            val emails = emailThreads.map {
                    it.latestEmail.isTrash = true
                    it.latestEmail
                }

            db.emailDao().update(emails)
        }
    }

}
