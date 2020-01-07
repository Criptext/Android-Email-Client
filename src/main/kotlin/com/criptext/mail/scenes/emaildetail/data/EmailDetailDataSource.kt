package com.criptext.mail.scenes.emaildetail.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.EmailDetailLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.EmailContactJoinDao
import com.criptext.mail.db.dao.EmailDao
import com.criptext.mail.db.dao.PendingEventDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.emaildetail.workers.*

/**
 * Created by sebas on 3/12/18.
 */

class EmailDetailDataSource(override val runner: WorkRunner,
                            private val pendingDao: PendingEventDao,
                            private val emailDao: EmailDao,
                            private val accountDao: AccountDao,
                            private val storage: KeyValueStorage,
                            private val emailContactDao: EmailContactJoinDao,
                            private val httpClient: HttpClient,
                            var activeAccount: ActiveAccount,
                            private val emailDetailLocalDB: EmailDetailLocalDB,
                            private val filesHttpClient: HttpClient,
                            private val downloadDir: String)
    : BackgroundWorkManager<EmailDetailRequest, EmailDetailResult>()
{

    override fun createWorkerFromParams(params: EmailDetailRequest,
                                        flushResults: (EmailDetailResult) -> Unit):
            BackgroundWorker<*> {
        return when (params) {
            is EmailDetailRequest.LoadFullEmailsFromThreadId -> LoadFullEmailsFromThreadWorker(
                    activeAccount = activeAccount,
                    db = emailDetailLocalDB,
                    threadId = params.threadId,
                    currentLabel = params.currentLabel,
                    changeAccountMessage = params.changeAccountMessage,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is EmailDetailRequest.UnsendFullEmailFromEmailId -> UnsendFullEmailWorker(
                    db = emailDetailLocalDB,
                    emailDao = emailDao,
                    emailContactDao = emailContactDao,
                    emailId = params.emailId,
                    position = params.position,
                    httpClient = httpClient,
                    activeAccount = activeAccount,
                    accountDao = accountDao,
                    storage = storage,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is EmailDetailRequest.GetSelectedLabels -> GetSelectedLabelsWorker(
                    db = emailDetailLocalDB,
                    activeAccount = activeAccount,
                    threadId = params.threadId,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is EmailDetailRequest.UpdateEmailThreadsLabelsRelations -> UpdateEmailThreadLabelsWorker(
                    pendingDao = pendingDao,
                    httpClient = httpClient,
                    activeAccount = activeAccount,
                    db = emailDetailLocalDB,
                    exitAndReload = params.exitAndReload,
                    threadId = params.threadId,
                    selectedLabels = params.selectedLabels,
                    currentLabel = params.currentLabel,
                    removeCurrentLabel = params.removeCurrentLabel,
                    storage = storage,
                    accountDao = accountDao,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is EmailDetailRequest.UpdateUnreadStatus -> UpdateUnreadStatusWorker(
                    db = emailDetailLocalDB,
                    pendingDao = pendingDao,
                    threadId = params.threadId,
                    updateUnreadStatus = params.updateUnreadStatus,
                    currentLabel = params.currentLabel,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    accountDao = accountDao,
                    storage = storage,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is EmailDetailRequest.MoveEmailThread -> MoveEmailThreadWorker(
                    chosenLabel = params.chosenLabel,
                    db = emailDetailLocalDB,
                    pendingDao = pendingDao,
                    threadId = params.threadId,
                    currentLabel = params.currentLabel,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    storage = storage,
                    accountDao = accountDao,
                    isPhishing = params.isPhishing,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is EmailDetailRequest.MoveEmail -> MoveEmailWorker(
                    chosenLabel = params.chosenLabel,
                    db = emailDetailLocalDB,
                    pendingDao = pendingDao,
                    emailId = params.emailId,
                    currentLabel = params.currentLabel,
                    activeAccount = activeAccount,
                    emailDao = emailDao,
                    httpClient = httpClient,
                    accountDao = accountDao,
                    storage = storage,
                    isPhishing = params.isPhishing,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is EmailDetailRequest.ReadEmails -> ReadEmailsWorker(
                    httpClient = httpClient,
                    dao = emailDao,
                    pendingDao = pendingDao,
                    activeAccount = activeAccount,
                    emailIds = params.emailIds,
                    metadataKeys = params.metadataKeys,
                    storage = storage,
                    accountDao = accountDao,
                    publishFn = { result ->
                        flushResults(result)
                    })
            is EmailDetailRequest.MarkAsReadEmail -> MarkAsReadEmailWorker(
                    httpClient = httpClient,
                    dao = emailDao,
                    pendingDao = pendingDao,
                    activeAccount = activeAccount,
                    metadataKeys= params.metadataKeys,
                    storage = storage,
                    accountDao = accountDao,
                    unread = params.unread,
                    publishFn = { result ->
                        flushResults(result)
                    })
            is EmailDetailRequest.DownloadFile -> DownloadAttachmentWorker(
                    fileName = params.fileName,
                    fileSize = params.fileSize,
                    fileToken = params.fileToken,
                    cid = params.cid,
                    emailId = params.emailId,
                    fileKey = params.fileKey,
                    downloadPath = downloadDir,
                    httpClient = filesHttpClient,
                    activeAccount = activeAccount,
                    storage = storage,
                    db = emailDetailLocalDB,
                    accountDao = accountDao,
                    publishFn = { result ->
                        flushResults(result)
                    })
            is EmailDetailRequest.CopyToDownloads -> CopyToDownloadWorker(
                    internalPath = params.internalPath,
                    publishFn = { result ->
                        flushResults(result)
                    })
            is EmailDetailRequest.DeleteDraft -> DeleteDraftWorker(
                    emailId = params.emailId,
                    activeAccount = activeAccount,
                    db = emailDetailLocalDB,
                    publishFn = { res -> flushResults(res) })
        }
    }
}
