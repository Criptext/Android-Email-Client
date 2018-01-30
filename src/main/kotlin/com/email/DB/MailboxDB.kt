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
    fun getEmailThreads(): ArrayList<EmailThread>
    fun seed()


    class Default(var applicationContext: Context): MailboxLocalDB {
        private var db = AppDatabase.getAppDatabase(applicationContext)

        override fun getEmailThreads(): ArrayList<EmailThread> {
            return db!!.emailDao().getAll().map { email ->
                EmailThread(latestMail = email,
                        labelsOfMail = db!!.emailLabelDao().getLabelsFromEmail(email.id) as ArrayList<Label>,
                        count = -1,
                        hasEmailAttachments = false)
            } as ArrayList<EmailThread>
        }

        override fun seed() {
            try {
                LabelSeeder.seed(db!!.labelDao())
                EmailSeeder.seed(db!!.emailDao())
                EmailLabelSeeder.seed(db!!.emailLabelDao())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
