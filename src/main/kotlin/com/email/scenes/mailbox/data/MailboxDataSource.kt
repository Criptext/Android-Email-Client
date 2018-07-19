package com.email.scenes.mailbox.data

import com.email.api.HttpClient
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.BackgroundWorkManager
import com.email.bgworker.WorkRunner
import com.email.db.MailboxLocalDB
import com.email.db.dao.*
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
        private val emailDao: EmailDao,
        private val fileDao: FileDao,
        private val fileKeyDao: FileKeyDao,
        private val labelDao: LabelDao,
        private val contactDao: ContactDao,
        private val emailLabelDao: EmailLabelDao,
        private val emailContactJoinDao: EmailContactJoinDao,
        private val feedItemDao: FeedItemDao,
        private val rawSessionDao: RawSessionDao,
        private val emailInsertionDao: EmailInsertionDao,
        private val httpClient: HttpClient,
        private val mailboxLocalDB: MailboxLocalDB )
    : BackgroundWorkManager<MailboxRequest, MailboxResult>() {
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
                    emailDao = emailDao,
                    dao = emailInsertionDao,
                    feedItemDao = feedItemDao,
                    contactDao = contactDao,
                    signalClient = signalClient,
                    db = mailboxLocalDB,
                    httpClient = httpClient,
                    activeAccount = activeAccount,
                    label = params.label,
                    loadedThreadsCount = params.loadedThreadsCount,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is MailboxRequest.LoadEmailThreads -> LoadEmailThreadsWorker(
                    db = mailboxLocalDB,
                    loadParams = params.loadParams,
                    labelNames = params.label,
                    userEmail = params.userEmail,
                    publishFn = { result ->
                        flushResults(result)
                    })
            is MailboxRequest.SendMail -> SendMailWorker(
                    signalClient = signalClient,
                    activeAccount = activeAccount,
                    rawSessionDao = rawSessionDao,
                    db = mailboxLocalDB,
                    httpClient = httpClient,
                    emailId = params.emailId,
                    threadId = params.threadId,
                    composerInputData = params.data,
                    attachments = params.attachments,
                    fileKey = params.fileKey,
                    publishFn = { res -> flushResults(res) })

            is MailboxRequest.UpdateEmailThreadsLabelsRelations -> UpdateEmailThreadsLabelsWorker(
                    db = mailboxLocalDB,
                    selectedThreadIds = params.selectedThreadIds,
                    selectedLabels = params.selectedLabels,
                    currentLabel = params.currentLabel,
                    shouldRemoveCurrentLabel = params.shouldRemoveCurrentLabel,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is MailboxRequest.MoveEmailThread -> MoveEmailThreadWorker(
                    chosenLabel = params.chosenLabel,
                    db = mailboxLocalDB,
                    selectedThreadIds = params.selectedThreadIds,
                    currentLabel = params.currentLabel,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is MailboxRequest.GetMenuInformation -> GetMenuInformationWorker(
                    db = mailboxLocalDB,
                    publishFn = { result ->
                        flushResults(result)
                    }
            )
            is MailboxRequest.UpdateUnreadStatus -> UpdateUnreadStatusWorker(
                    db = mailboxLocalDB,
                    threadIds = params.threadIds,
                    updateUnreadStatus = params.updateUnreadStatus,
                    currentLabel = params.currentLabel,
                    publishFn = { result ->
                        flushResults(result)
                    }
            )

            is MailboxRequest.LinkDevice -> LinkDevicesWorker(
                    emailDao = emailDao,
                    contactDao = contactDao,
                    fileDao = fileDao,
                    fileKeyDao = fileKeyDao,
                    labelDao = labelDao,
                    emailLabelDao = emailLabelDao,
                    emailContactJoinDao = emailContactJoinDao,
                    signalClient = signalClient,
                    httpClient = httpClient,
                    activeAccount = activeAccount,
                    publishFn = { result ->
                        flushResults(result)
            })
        }
    }

}
