package com.email.scenes.mailbox.data

import com.email.DB.MailboxLocalDB
import com.email.DB.models.Label
import com.email.scenes.LabelChooser.data.LabelThread

/**
 * Created by sebas on 1/24/18.
 */

class MailboxDataSource(private val mailboxLocalDB: MailboxLocalDB) {

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

    fun updateUnreadStatus(emailThreads: List<EmailThread>,
                         updateUnreadStatus: Boolean
                         ) {
        mailboxLocalDB.updateUnreadStatus(emailThreads,
                updateUnreadStatus)

    }
    fun createLabelEmailRelation(labelId: Int, emailId: Int) {
        return mailboxLocalDB.createLabelEmailRelation(labelId, emailId)
    }

    fun  moveSelectedEmailThreadsToTrash(emailThreads: List<EmailThread>) {
        mailboxLocalDB.moveSelectedEmailThreadsToTrash(emailThreads)
    }

    fun  moveSelectedEmailThreadsToSpam(emailThreads: List<EmailThread>) {
        mailboxLocalDB.moveSelectedEmailThreadsToSpam(emailThreads)
    }
}
