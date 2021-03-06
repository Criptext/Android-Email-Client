package com.criptext.mail.scenes.composer.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.ComposerLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.EmailInsertionDao
import com.criptext.mail.db.dao.PendingEventDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.composer.workers.*
import com.criptext.mail.utils.generaldatasource.workers.GetRemoteFileWorker
import java.io.File

/**
 * Created by gabriel on 2/26/18.
 */

class ComposerDataSource(
        private val filesDir: File,
        private val httpClient: HttpClient,
        private val composerLocalDB: ComposerLocalDB,
        private val pendingEventDao: PendingEventDao,
        var activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private val emailInsertionDao: EmailInsertionDao,
        override val runner: WorkRunner)
    : BackgroundWorkManager<ComposerRequest, ComposerResult>() {

    override fun createWorkerFromParams(params: ComposerRequest,
                                        flushResults: (ComposerResult) -> Unit)
            : BackgroundWorker<*> {
        return when(params) {
            is ComposerRequest.GetAllContacts -> LoadContactsWorker(
                    composerLocalDB
            ) { res ->
                flushResults(res)
            }
            is ComposerRequest.GetAllFromAddresses -> LoadFromAddressesWorker(
                    composerLocalDB,
                    activeAccount
            ) { res ->
                flushResults(res)
            }
            is ComposerRequest.SaveEmailAsDraft -> SaveEmailWorker(
                    goToRecoveryEmail = params.goToRecoveryEmail,
                    threadId = params.threadId,
                    emailId = params.emailId, composerInputData = params.composerInputData,
                    senderAddress = params.senderEmail, dao = emailInsertionDao,
                    filesDir = filesDir, activeAccount = activeAccount,
                    onlySave = params.onlySave, attachments = params.attachments,
                    publishFn = { res -> flushResults(res) }, fileKey = params.fileKey,
                    originalId = params.originalId,
                    currentLabel = params.currentLabel,
                    db = composerLocalDB)
            is ComposerRequest.UploadAttachment -> UploadAttachmentWorker(filesSize = params.filesSize,
                    filepath = params.filepath,
                    httpClient = httpClient, activeAccount = activeAccount,
                    publishFn = { res -> flushResults(res) }, fileKey = params.fileKey,
                    accountDao = composerLocalDB.accountDao, storage = storage, uuid = params.uuid,
                    groupId = params.groupId)
            is ComposerRequest.LoadInitialData -> LoadInitialDataWorker(
                    httpClient = HttpClient.Default(),
                    db = composerLocalDB,
                    emailId = params.emailId,
                    composerType = params.composerType,
                    userEmailAddress = activeAccount.userEmail,
                    signature = activeAccount.signature,
                    activeAccount = activeAccount,
                    storage = storage,
                    pendingEventDao = pendingEventDao,
                    publishFn = { res -> flushResults(res) })
            is ComposerRequest.CheckDomain -> CheckDomainsWorker(
                    emails = params.emails,
                    httpClient = HttpClient.Default(),
                    activeAccount = activeAccount,
                    publishFn = { res -> flushResults(res) }
            )
            is ComposerRequest.CheckCanSend -> CheckCanSendWorker(
                    composerInputData = params.composerInputData,
                    httpClient = HttpClient.Default(),
                    activeAccount = activeAccount,
                    publishFn = { res -> flushResults(res) }
            )
            is ComposerRequest.SwitchActiveAccount -> SwitchActiveAccountWorker(
                    db = composerLocalDB,
                    newAccountAddress = params.new,
                    oldAccountAddress = params.old,
                    storage = storage,
                    activeAccount = activeAccount,
                    publishFn = { res -> flushResults(res) }
            )
        }
    }

}