package com.criptext.mail.scenes.mailbox.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.dao.*
import com.criptext.mail.db.dao.signal.RawIdentityKeyDao
import com.criptext.mail.db.dao.signal.RawSessionDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.mailbox.workers.*
import com.criptext.mail.signal.SignalClient

/**
 * Created by sebas on 1/24/18.
 */

class MailboxDataSource(
        private val signalClient: SignalClient,
        private val storage: KeyValueStorage,
        override val runner: WorkRunner,
        private val activeAccount: ActiveAccount,
        private val pendingDao: PendingEventDao,
        private val accountDao: AccountDao,
        private val emailDao: EmailDao,
        private val fileDao: FileDao,
        private val fileKeyDao: FileKeyDao,
        private val labelDao: LabelDao,
        private val contactDao: ContactDao,
        private val emailLabelDao: EmailLabelDao,
        private val emailContactJoinDao: EmailContactJoinDao,
        private val feedItemDao: FeedItemDao,
        private val rawSessionDao: RawSessionDao,
        private val rawIdentityKeyDao: RawIdentityKeyDao,
        private val emailInsertionDao: EmailInsertionDao,
        private val httpClient: HttpClient,
        private val mailboxLocalDB: MailboxLocalDB,
        private val eventLocalDB: EventLocalDB)
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

            is MailboxRequest.LoadEmailThreads -> LoadEmailThreadsWorker(
                    db = mailboxLocalDB,
                    loadParams = params.loadParams,
                    labelNames = params.label,
                    userEmail = params.userEmail,
                    publishFn = { result ->
                        flushResults(result)
                    })
            is MailboxRequest.SendMail -> SendMailWorker(
                    storage = storage,
                    accountDao = accountDao,
                    signalClient = signalClient,
                    activeAccount = activeAccount,
                    rawSessionDao = rawSessionDao,
                    rawIdentityKeyDao = rawIdentityKeyDao,
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
                    pendingDao = pendingDao,
                    selectedThreadIds = params.selectedThreadIds,
                    selectedLabels = params.selectedLabels,
                    currentLabel = params.currentLabel,
                    shouldRemoveCurrentLabel = params.shouldRemoveCurrentLabel,
                    httpClient = httpClient,
                    activeAccount = activeAccount,
                    accountDao = accountDao,
                    storage = storage,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is MailboxRequest.MoveEmailThread -> MoveEmailThreadWorker(
                    chosenLabel = params.chosenLabel,
                    pendingDao = pendingDao,
                    db = mailboxLocalDB,
                    selectedThreadIds = params.selectedThreadIds,
                    currentLabel = params.currentLabel,
                    httpClient = httpClient,
                    activeAccount = activeAccount,
                    storage = storage,
                    accountDao = accountDao,
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
                    pendingDao = pendingDao,
                    threadIds = params.threadIds,
                    updateUnreadStatus = params.updateUnreadStatus,
                    currentLabel = params.currentLabel,
                    httpClient = httpClient,
                    activeAccount = activeAccount,
                    accountDao = accountDao,
                    storage = storage,
                    publishFn = { result ->
                        flushResults(result)
                    }
            )

            is MailboxRequest.GetEmailPreview -> GetEmailPreviewWorker(
                    threadId = params.threadId,
                    mailboxLocalDB = mailboxLocalDB,
                    userEmail = params.userEmail,
                    doReply = params.doReply,
                    publishFn = { result ->
                        flushResults(result)
                    })
            is MailboxRequest.EmptyTrash -> EmptyTrashWorker(
                    db = mailboxLocalDB,
                    pendingDao = pendingDao,
                    httpClient = httpClient,
                    activeAccount = activeAccount,
                    storage = storage,
                    accountDao = accountDao,
                    publishFn = { result ->
                        flushResults(result)
                    })
            is MailboxRequest.ResendEmails -> ResendEmailsWorker(
                    accountDao = accountDao,
                    storage = storage,
                    rawSessionDao = rawSessionDao,
                    signalClient = signalClient,
                    db = mailboxLocalDB,
                    httpClient = httpClient,
                    activeAccount = activeAccount,
                    publishFn = { result ->
                        flushResults(result)
                    })
            is MailboxRequest.GetPendingLinkRequest -> GetPendingLinkRequestWorker(
                    storage = storage,
                    accountDao = accountDao,
                    httpClient = httpClient,
                    activeAccount = activeAccount,
                    publishFn = { result ->
                        flushResults(result)
                    })
            is MailboxRequest.ResendPeerEvents -> ResendPeerEventsWorker(
                    pendingDao = pendingDao,
                    httpClient = httpClient,
                    activeAccount = activeAccount,
                    accountDao = accountDao,
                    storage = storage,
                    publishFn = { result ->
                        flushResults(result)
                    })
        }
    }

}
