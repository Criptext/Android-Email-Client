package com.email.views.mailbox.data

import com.email.DB.AppDatabase
import com.email.DB.MailboxLocalDB
import com.email.DB.models.Email
import com.email.DB.models.Label

/**
 * Created by sebas on 1/24/18.
 */

class MailboxDataSource(private val mailboxLocalDB: MailboxLocalDB) {

    fun getEmailThreads(): ArrayList<EmailThread> {
        return mailboxLocalDB.getEmailThreads()
    }
}
