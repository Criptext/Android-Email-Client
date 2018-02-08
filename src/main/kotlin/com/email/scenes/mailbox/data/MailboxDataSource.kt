package com.email.scenes.mailbox.data

import com.email.DB.MailboxLocalDB
import com.email.DB.models.Label
import com.email.scenes.LabelChooser.data.LabelThread

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

    fun getAllLabels(): List<LabelThread> {
        return mailboxLocalDB.getAllLabels()
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

    fun deleteEmailThreads(emailThreads: List<EmailThread>) {
        mailboxLocalDB.deleteEmailThreads(emailThreads)
    }

    fun changeMessagesToRead(emailThreads: List<EmailThread>) {
        mailboxLocalDB.changeMessagesToRead(emailThreads)
    }

    fun changeMessagesToUnRead(emailThreads: List<EmailThread>) {
        mailboxLocalDB.changeMessagesToUnRead(emailThreads)
    }

    fun createLabelEmailRelation(labelId: Int, emailId: Int) {
        return mailboxLocalDB.createLabelEmailRelation(labelId, emailId)
    }

}
