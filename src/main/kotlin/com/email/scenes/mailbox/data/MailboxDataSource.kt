package com.email.scenes.mailbox.data

import com.email.DB.MailboxLocalDB
import com.email.DB.models.Label

/**
 * Created by sebas on 1/24/18.
 */

class MailboxDataSource(private val mailboxLocalDB: MailboxLocalDB) {

    fun getAllEmailThreads(): List<EmailThread> {
        return mailboxLocalDB.getAllEmailThreads()
    }

    fun getNotArchivedEmailThreads(): List<EmailThread> {
        return mailboxLocalDB.getNotArchivedEmailThreads()
    }

    fun getArchivedEmailThreads(): List<EmailThread> {
        return mailboxLocalDB.getArchivedEmailThreads()
    }

    fun removeLabelsRelation(labels: List<Label>, emailId: Int) {
        return mailboxLocalDB.removeLabelsRelation(labels, emailId)
    }
    fun seed() {
        mailboxLocalDB.seed()
    }
}
