package com.email.scenes.mailbox.data

import com.email.bgworker.BackgroundWorker
import com.email.bgworker.WorkHandler
import com.email.bgworker.WorkRunner
import com.email.db.MailboxLocalDB
import com.email.db.dao.EmailInsertionDao
import com.email.db.dao.signal.RawSessionDao
import com.email.db.models.ActiveAccount
import com.email.signal.SignalClient

/**
 * Created by sebas on 1/24/18.
 */

class MailboxDataSource(
        private val signalClient: SignalClient,
        override val runner: WorkRunner,
        private val activeAccount: ActiveAccount,
        private val rawSessionDao: RawSessionDao,
        private val emailInsertionDao: EmailInsertionDao,
        private val mailboxLocalDB: MailboxLocalDB )
    : WorkHandler<MailboxRequest, MailboxResult>() {
    override fun createWorkerFromParams(
            params: MailboxRequest,
            flushResults: (MailboxResult) -> Unit)
            : BackgroundWorker<*> {
        return when (params) {
            is MailboxRequest.GetSelectedLabels -> GetSelectedLabelsWorker(
                    db = mailboxLocalDB,
                    threadIds = params.threadIds,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is MailboxRequest.UpdateMailbox -> UpdateMailboxWorker(
                    dao = emailInsertionDao,
                    signalClient = signalClient,
                    db = mailboxLocalDB,
                    activeAccount = activeAccount,
                    label = params.label,
                    loadedThreadsCount = params.loadedThreadsCount,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is MailboxRequest.LoadEmailThreads -> LoadEmailThreadsWorker(
                    db = mailboxLocalDB,
                    loadParams = params.loadParams,
                    labelTextTypes = params.label,
                    publishFn = { result ->
                        flushResults(result)
                    })
            is MailboxRequest.SendMail -> SendMailWorker(
                    signalClient = signalClient,
                    activeAccount = activeAccount,
                    rawSessionDao = rawSessionDao,
                    emailId = params.emailId,
                    threadId = params.threadId,
                    composerInputData = params.data,
                    publishFn = { res -> flushResults(res) })

            is MailboxRequest.UpdateEmailThreadsLabelsRelations -> UpdateEmailThreadsLabelsRelationsWorker(
                    chosenLabel = params.chosenLabel,
                    db = mailboxLocalDB,
                    selectedEmailThreads = params.selectedEmailThreads,
                    selectedLabels = params.selectedLabels,
                    publishFn = { result ->
                        flushResults(result)
                    })
            is MailboxRequest.UpdateEmail -> UpdateEmailWorker(
                    db = mailboxLocalDB,
                    emailId = params.emailId,
                    response = params.response,
                    publishFn = { result ->
                        flushResults(result)
                    }
            )
            is MailboxRequest.GetMenuInformation -> GetMenuInformationWorker(
                    db = mailboxLocalDB,
                    publishFn = { result ->
                        flushResults(result)
                    }
            )
            is MailboxRequest.UpdateUnreadStatus -> UpdateUnreadStatusWorker(
                    db = mailboxLocalDB,
                    emailThreads = params.emailThreads,
                    updateUnreadStatus = params.updateUnreadStatus,
                    publishFn = { result ->
                        flushResults(result)
                    }
            )
        }
    }

}
