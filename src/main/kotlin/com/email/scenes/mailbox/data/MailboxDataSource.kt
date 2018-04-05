package com.email.scenes.mailbox.data

import com.email.bgworker.BackgroundWorker
import com.email.bgworker.WorkHandler
import com.email.bgworker.WorkRunner
import com.email.db.MailboxLocalDB
import com.email.db.models.ActiveAccount
import com.email.db.models.Label
import com.email.scenes.labelChooser.data.LabelWrapper
import com.email.signal.SignalClient

/**
 * Created by sebas on 1/24/18.
 */

class MailboxDataSource(
        private val signalClient: SignalClient,
        override val runner: WorkRunner,
        private val activeAccount: ActiveAccount,
        private val mailboxLocalDB: MailboxLocalDB )
    : WorkHandler<MailboxRequest, MailboxResult>() {
    override fun createWorkerFromParams(
            params: MailboxRequest,
            flushResults: (MailboxResult) -> Unit)
            : BackgroundWorker<*> {

        return when (params) {
            is MailboxRequest.GetLabels -> GetLabelsWorker(
                    db = mailboxLocalDB,
                    activeAccount = activeAccount,
                    threadIds = params.threadIds,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is MailboxRequest.UpdateMailbox -> UpdateMailboxWorker(
                    signalClient = signalClient,
                    db = mailboxLocalDB,
                    activeAccount = activeAccount,
                    label = params.label,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is MailboxRequest.LoadEmailThreads -> LoadEmailThreadsWorker(
                    db = mailboxLocalDB,
                    activeAccount = activeAccount,
                    labelTextTypes = params.label,
                    offset = params.offset,
                    oldestEmailThread = params.oldestEmailThread,
                    publishFn = { result ->
                        flushResults(result)
                    })

        }
    }

    fun getNotArchivedEmailThreads(): List<EmailThread> {
        return mailboxLocalDB.getNotArchivedEmailThreads()
    }

    fun getAllLabels(): List<LabelWrapper> {
        return mailboxLocalDB.getAllLabelWrappers()
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

    interface Listener {
        fun onMailboxUpdated(resultData: MailboxResult.UpdateMailbox)
    }
}
