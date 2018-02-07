package com.email.scenes.mailbox.data

import com.email.DB.MailboxLocalDB
import com.email.DB.models.Label
import com.email.scenes.LabelChooser.data.LabelThread

/**
 * Created by sebas on 1/24/18.
 */

class MailboxDataSource(private val mailboxLocalDB: MailboxLocalDB) {

    fun getAllEmailThreads(): ArrayList<EmailThread> {
        return mailboxLocalDB.getAllEmailThreads()
    }

    fun getNotArchivedEmailThreads(): ArrayList<EmailThread> {
        return mailboxLocalDB.getNotArchivedEmailThreads()
    }

    fun getAllLabels(): ArrayList<LabelThread> {
        return mailboxLocalDB.getAllLabels()
    }

    fun getArchivedEmailThreads(): ArrayList<EmailThread> {
        return mailboxLocalDB.getArchivedEmailThreads()
    }

    fun removeLabelsRelation(labels: ArrayList<Label>, emailId: Int) {
        return mailboxLocalDB.removeLabelsRelation(labels, emailId)
    }
    fun seed() {
        mailboxLocalDB.seed()
    }

    fun deleteEmailThreads(emailThreads: ArrayList<EmailThread>) {
        mailboxLocalDB.deleteEmailThreads(emailThreads)
    }

    fun createLabelEmailRelation(labelId: Int, emailId: Int) {
        return mailboxLocalDB.createLabelEmailRelation(labelId, emailId)
    }

}
