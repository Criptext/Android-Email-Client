package com.criptext.mail.scenes.composer.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.ComposerLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.EmailInsertionDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.composer.workers.*

/**
 * Created by gabriel on 2/26/18.
 */

class ComposerDataSource(
        private val httpClient: HttpClient,
        private val composerLocalDB: ComposerLocalDB,
        private val activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private val emailInsertionDao: EmailInsertionDao,
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
                    publishFn = { res -> flushResults(res) }, fileKey = params.fileKey,
                    originalId = params.originalId)

            is ComposerRequest.DeleteDraft -> DeleteDraftWorker(
                    emailId = params.emailId,
                    db = composerLocalDB,
                    publishFn = { res -> flushResults(res) })
            is ComposerRequest.UploadAttachment -> UploadAttachmentWorker(filepath = params.filepath,
                    httpClient = httpClient, activeAccount = activeAccount,
                    publishFn = { res -> flushResults(res) }, fileKey = params.fileKey,
                    accountDao = composerLocalDB.accountDao, storage = storage)
            is ComposerRequest.LoadInitialData -> LoadInitialDataWorker(db = composerLocalDB,
                    emailId = params.emailId,
                    composerType = params.composerType,
                    userEmailAddress = activeAccount.userEmail,
                    signature = activeAccount.signature,
                    publishFn = { res -> flushResults(res) })
            is ComposerRequest.GetRemoteFile -> GetRemoteFileWorker(
                    uris = params.uris,
                    contentResolver = params.contentResolver,
                    publishFn = { res -> flushResults(res) }
            )
        }
    }

}