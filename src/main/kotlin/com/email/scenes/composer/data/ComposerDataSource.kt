package com.email.scenes.composer.data

import com.email.api.HttpClient
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.BackgroundWorkManager
import com.email.bgworker.WorkRunner
import com.email.db.ComposerLocalDB
import com.email.db.dao.EmailInsertionDao
import com.email.db.models.ActiveAccount

/**
 * Created by gabriel on 2/26/18.
 */

class ComposerDataSource(
        private val httpClient: HttpClient,
        private val composerLocalDB: ComposerLocalDB,
        private val activeAccount: ActiveAccount,
        private val emailInsertionDao: EmailInsertionDao,
        private val authToken: String,
        override val runner: WorkRunner)
    : BackgroundWorkManager<ComposerRequest, ComposerResult>() {

    override fun createWorkerFromParams(params: ComposerRequest,
                                        flushResults: (ComposerResult) -> Unit)
            : BackgroundWorker<*> {
        return when(params) {
            is ComposerRequest.GetAllContacts -> LoadContactsWorker(composerLocalDB, { res ->
                flushResults(res)
            })
            is ComposerRequest.SaveEmailAsDraft -> SaveEmailWorker(
                    threadId = params.threadId,
                    emailId = params.emailId, composerInputData = params.composerInputData,
                    account = activeAccount, dao = emailInsertionDao,
                    onlySave = params.onlySave, attachments = params.attachments,
                    publishFn = { res -> flushResults(res) })
            is ComposerRequest.DeleteDraft -> DeleteDraftWorker(
                    emailId = params.emailId,
                    db = composerLocalDB,
                    publishFn = { res -> flushResults(res) })
            is ComposerRequest.UploadAttachment -> UploadAttachmentWorker(filepath = params.filepath,
                    httpClient = httpClient, fileServiceAuthToken = authToken,
                    publishFn = { res -> flushResults(res)})
            is ComposerRequest.LoadInitialData -> LoadInitialDataWorker(db = composerLocalDB,
                    emailId = params.emailId,
                    composerType = params.composerType,
                    userEmailAddress = activeAccount.userEmail,
                    signature = activeAccount.signature,
                    publishFn = { res -> flushResults(res)})
        }
    }

}