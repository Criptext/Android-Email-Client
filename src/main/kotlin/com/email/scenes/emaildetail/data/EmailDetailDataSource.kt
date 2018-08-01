package com.email.scenes.emaildetail.data

import com.email.api.HttpClient
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.BackgroundWorkManager
import com.email.bgworker.WorkRunner
import com.email.db.EmailDetailLocalDB
import com.email.db.dao.EmailContactJoinDao
import com.email.db.dao.EmailDao
import com.email.db.models.ActiveAccount
import com.email.scenes.emaildetail.workers.*
import com.email.signal.SignalClient

/**
 * Created by sebas on 3/12/18.
 */

class EmailDetailDataSource(override val runner: WorkRunner,
                            private val emailDao: EmailDao,
                            private val emailContactDao: EmailContactJoinDao,
                            private val httpClient: HttpClient,
                            private val activeAccount: ActiveAccount,
                            private val emailDetailLocalDB: EmailDetailLocalDB,
                            private val filesHttpClient: HttpClient,
                            private val fileServiceAuthToken: String,
                            private val downloadDir: String)
    : BackgroundWorkManager<EmailDetailRequest, EmailDetailResult>()
{

    override fun createWorkerFromParams(params: EmailDetailRequest,
                                        flushResults: (EmailDetailResult) -> Unit):
            BackgroundWorker<*> {
        return when (params) {
            is EmailDetailRequest.LoadFullEmailsFromThreadId -> LoadFullEmailsFromThreadWorker(
                    db = emailDetailLocalDB,
                    threadId = params.threadId,
                    currentLabel = params.currentLabel,
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
                    publishFn = { result ->
                        flushResults(result)
                    })

            is EmailDetailRequest.GetSelectedLabels -> GetSelectedLabelsWorker(
                    db = emailDetailLocalDB,
                    threadId = params.threadId,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is EmailDetailRequest.UpdateEmailThreadsLabelsRelations -> UpdateEmailThreadLabelsWorker(
                    db = emailDetailLocalDB,
                    threadId = params.threadId,
                    selectedLabels = params.selectedLabels,
                    currentLabel = params.currentLabel,
                    removeCurrentLabel = params.removeCurrentLabel,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is EmailDetailRequest.UpdateUnreadStatus -> UpdateUnreadStatusWorker(
                    db = emailDetailLocalDB,
                    threadId = params.threadId,
                    updateUnreadStatus = params.updateUnreadStatus,
                    currentLabel = params.currentLabel,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is EmailDetailRequest.MoveEmailThread -> MoveEmailThreadWorker(
                    chosenLabel = params.chosenLabel,
                    db = emailDetailLocalDB,
                    threadId = params.threadId,
                    currentLabel = params.currentLabel,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is EmailDetailRequest.MoveEmail -> MoveEmailWorker(
                    chosenLabel = params.chosenLabel,
                    db = emailDetailLocalDB,
                    emailId = params.emailId,
                    currentLabel = params.currentLabel,
                    activeAccount = activeAccount,
                    emailDao = emailDao,
                    httpClient = httpClient,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is EmailDetailRequest.ReadEmails -> ReadEmailsWorker(
                    httpClient = httpClient,
                    dao = emailDao,
                    activeAccount = activeAccount,
                    emailIds = params.emailIds,
                    metadataKeys = params.metadataKeys,
                    publishFn = { result ->
                        flushResults(result)
                    })
            is EmailDetailRequest.DownloadFile -> DownloadAttachmentWorker(
                    fileToken = params.fileToken,
                    emailId = params.emailId,
                    fileKey = params.fileKey,
                    downloadPath = downloadDir,
                    httpClient = filesHttpClient,
                    fileServiceAuthToken = fileServiceAuthToken,
                    publishFn = { result ->
                        flushResults(result)
                    })
        }
    }
}
