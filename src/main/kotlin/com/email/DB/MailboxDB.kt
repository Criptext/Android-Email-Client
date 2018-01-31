package com.email.DB

import android.content.Context
import com.email.DB.models.Label
import com.email.DB.seeders.EmailLabelSeeder
import com.email.DB.seeders.EmailSeeder
import com.email.DB.seeders.LabelSeeder
import com.email.scenes.mailbox.data.EmailThread

/**
 * Created by sebas on 1/26/18.
 */

interface MailboxLocalDB {
    fun getEmailThreads(): List<EmailThread>
    fun seed()


    class Default(val applicationContext: Context): MailboxLocalDB {
        private val db = AppDatabase.getAppDatabase(applicationContext)

        override fun getEmailThreads(): List<EmailThread> {
            return db!!.emailDao().getAll().map { email ->
                EmailThread(email = email,
                        labelsOfMail = db.emailLabelDao().getLabelsFromEmail(email.id) as ArrayList<Label>)
            }
        }

        override fun seed() {
            try {
                LabelSeeder.seed(db!!.labelDao())
                EmailSeeder.seed(db.emailDao())
                EmailLabelSeeder.seed(db.emailLabelDao())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
