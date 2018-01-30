package com.email.scenes.mailbox.data

import com.email.DB.MailboxLocalDB

/**
 * Created by sebas on 1/24/18.
 */

class MailboxDataSource(private val mailboxLocalDB: MailboxLocalDB) {

    fun getEmailThreads(): ArrayList<EmailThread> {
        return mailboxLocalDB.getEmailThreads()
    }

    fun seed() {
        mailboxLocalDB.seed()
    }
}
