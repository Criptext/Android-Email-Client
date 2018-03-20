package com.email.scenes.mailbox.data

import com.email.bgworker.BackgroundWorker
import com.email.bgworker.WorkHandler
import com.email.bgworker.WorkRunner
import com.email.db.MailboxLocalDB
import com.email.db.models.Label
import com.email.scenes.labelChooser.data.LabelThread

/**
 * Created by sebas on 1/24/18.
 */

class MailboxDataSource(
        override val runner: WorkRunner,
        private val mailboxAPIClient: MailboxAPIClient?,
        private val mailboxLocalDB: MailboxLocalDB )
    : WorkHandler<MailboxRequest, MailboxResult>() {
    override fun createWorkerFromParams(
            params: MailboxRequest,
            flushResults: (MailboxResult) -> Unit)
            : BackgroundWorker<*> {

        return when (params) {
            is MailboxRequest.
            GetLabels -> GetLabelsWorker(
                    db = mailboxLocalDB,
                    apiClient = mailboxAPIClient,
                    threadIds = params.threadIds,
                    publishFn = { result ->
                        flushResults(result)
                    })
        }
    }

    fun getNotArchivedEmailThreads(): List<EmailThread> {
        return mailboxLocalDB.getNotArchivedEmailThreads()
    }

    fun getAllLabels(): List<LabelThread> {
        return mailboxLocalDB.getAllLabelThreads()
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
