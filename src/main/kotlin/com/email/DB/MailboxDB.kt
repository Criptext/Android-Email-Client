package com.email.DB

import android.content.Context
import com.email.DB.models.Email
import com.email.DB.models.EmailLabel
import com.email.DB.models.Label
import com.email.DB.seeders.EmailLabelSeeder
import com.email.DB.seeders.EmailSeeder
import com.email.DB.seeders.LabelSeeder
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.LabelChooser.data.LabelThread

/**
 * Created by sebas on 1/26/18.
 */

interface MailboxLocalDB {
    fun getAllEmailThreads(): ArrayList<EmailThread>
    fun getArchivedEmailThreads(): ArrayList<EmailThread>
    fun getAllLabels(): ArrayList<LabelThread>
    fun getNotArchivedEmailThreads(): ArrayList<EmailThread>
    fun removeLabelsRelation(labels: ArrayList<Label>, emailId: Int)
    fun seed()
    fun deleteEmailThreads(emailThreads: ArrayList<EmailThread>)
    fun createLabelEmailRelation(labelId: Int, emailId: Int)


    class Default(applicationContext: Context): MailboxLocalDB {

        override fun createLabelEmailRelation(labelId: Int, emailId: Int) {
            val emailLabel = EmailLabel(labelId = labelId, emailId = emailId)
            return db.emailLabelDao().insert(emailLabel)
        }

        private val db = AppDatabase.getAppDatabase(applicationContext)

        override fun getAllEmailThreads(): ArrayList<EmailThread> {
            return db.emailDao().getAll().map { email ->
                EmailThread(email = email,
                        labelsOfMail = db.emailLabelDao().getLabelsFromEmail(email.id) as ArrayList<Label>)
            } as ArrayList<EmailThread>
        }

        override fun getArchivedEmailThreads(): ArrayList<EmailThread> {
            return db.emailDao().getAll().map { email ->
                EmailThread(email = email,
                        labelsOfMail = ArrayList())
            } as ArrayList<EmailThread>
        }

        override fun getNotArchivedEmailThreads(): ArrayList<EmailThread> {
            return db.emailDao().getNotArchivedEmailThreads().map { email ->
                EmailThread(email = email,
                        labelsOfMail = db.emailLabelDao().getLabelsFromEmail(email.id) as ArrayList<Label>)
            } as ArrayList<EmailThread>
        }

        override fun removeLabelsRelation(labels: ArrayList<Label>, emailId: Int) {
            labels.forEach{
                db.emailLabelDao().deleteByEmailLabelIds(it.id, emailId)
            }
        }

        override fun seed() {
            try {
                LabelSeeder.seed(db.labelDao())
                EmailSeeder.seed(db.emailDao())
                EmailLabelSeeder.seed(db.emailLabelDao())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun deleteEmailThreads(emailThreads: ArrayList<EmailThread>) {
            val emails: List<Email> = emailThreads.map {
                it.email
            }
            db.emailDao().deleteAll(emails)
        }

        override fun getAllLabels(): ArrayList<LabelThread> {
            return db.labelDao().getAll().map{ label ->
                LabelThread(label)
            } as ArrayList<LabelThread>
        }
    }

}
