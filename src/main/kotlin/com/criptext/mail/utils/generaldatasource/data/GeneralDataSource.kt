package com.criptext.mail.utils.generaldatasource.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.utils.generaldatasource.workers.*

class GeneralDataSource(override val runner: WorkRunner,
                        private val signalClient: SignalClient,
                        private val eventLocalDB: EventLocalDB,
                        private val db : AppDatabase,
                        private val storage: KeyValueStorage,
                        private val activeAccount: ActiveAccount?,
                        private val httpClient: HttpClient
): BackgroundWorkManager<GeneralRequest, GeneralResult>() {

    override fun createWorkerFromParams(params: GeneralRequest, flushResults: (GeneralResult) -> Unit): BackgroundWorker<*> {
        return when (params) {
            is GeneralRequest.DeviceRemoved -> DeviceRemovedWorker(
                    letAPIKnow = params.letAPIKnow,
                    activeAccount = activeAccount ?: ActiveAccount.loadFromStorage(storage)!!,
                    httpClient = httpClient,
                    db = db, storage = storage, publishFn = flushResults
            )
            is GeneralRequest.ConfirmPassword -> ConfirmPasswordWorker(
                    activeAccount = activeAccount!!, httpClient = httpClient, storage = storage,
                    accountDao = db.accountDao(),
                    password = params.password, publishFn = flushResults
            )
            is GeneralRequest.ResetPassword -> ForgotPasswordWorker(
                    recipientId = params.recipientId,
                    httpClient = httpClient,
                    publishFn = { res -> flushResults(res) }
            )
            is GeneralRequest.UpdateMailbox -> UpdateMailboxWorker(
                    signalClient = signalClient,
                    dbEvents = eventLocalDB,
                    httpClient = httpClient,
                    activeAccount = activeAccount!!,
                    label = params.label,
                    loadedThreadsCount = params.loadedThreadsCount,
                    storage = storage,
                    accountDao = db.accountDao(),
                    pendingEventDao = db.pendingEventDao(),
                    publishFn = { res -> flushResults(res) })
            is GeneralRequest.LinkAccept -> LinkAuthAcceptWorker(
                    activeAccount = activeAccount!!, httpClient = httpClient, accountDao = db.accountDao(),
                    untrustedDeviceInfo = params.untrustedDeviceInfo, storage = storage,
                    publishFn = { res -> flushResults(res)}
            )
            is GeneralRequest.LinkDenied -> LinkAuthDenyWorker(
                    activeAccount = activeAccount!!, httpClient = httpClient,
                    untrustedDeviceInfo = params.untrustedDeviceInfo, accountDao = db.accountDao(),
                    storage = storage,
                    publishFn = { res -> flushResults(res)}
            )
            is GeneralRequest.DataFileCreation -> DataFileCreationWorker(
                    db = db,
                    publishFn = { res -> flushResults(res)}
            )
            is GeneralRequest.PostUserData -> PostUserWorker(
                    httpClient = httpClient,
                    activeAccount = activeAccount!!,
                    randomId = params.randomId,
                    filePath = params.filePath,
                    deviceId = params.deviceID,
                    fileKey = params.key,
                    keyBundle = params.keyBundle,
                    signalClient = signalClient,
                    accountDao = db.accountDao(),
                    storage = storage,
                    publishFn = { res -> flushResults(res)}
            )
            is GeneralRequest.TotalUnreadEmails -> GetTotalUnreadMailsByLabelWorker(
                    emailDao = db.emailDao(),
                    currentLabel = params.currentLabel,
                    publishFn = { res -> flushResults(res)}
            )
            is GeneralRequest.SyncPhonebook -> SyncPhonebookWorker(
                    contactDao = db.contactDao(),
                    contentResolver = params.contentResolver,
                    publishFn = { res -> flushResults(res)}
            )
            is GeneralRequest.Logout -> LogoutWorker(
                    shouldDeleteAllData = params.shouldDeleteAllData,
                    db = eventLocalDB,
                    httpClient = httpClient,
                    activeAccount = activeAccount!!,
                    storage = storage,
                    accountDao = db.accountDao(),
                    publishFn = { res -> flushResults(res)}
            )
            is GeneralRequest.DeleteAccount -> DeleteAccountWorker(
                    password = params.password,
                    db = eventLocalDB,
                    httpClient = httpClient,
                    activeAccount = activeAccount!!,
                    storage = storage,
                    accountDao = db.accountDao(),
                    publishFn = { res -> flushResults(res)}
            )
            is GeneralRequest.SetReadReceipts -> ReadReceiptsWorker(
                    activeAccount = activeAccount!!,
                    readReceipts = params.readReceipts,
                    httpClient = httpClient,
                    accountDao = db.accountDao(),
                    storage = storage,
                    publishFn = { res -> flushResults(res) }
            )
            is GeneralRequest.CheckForKeyBundle -> CheckForKeyBundleWorker(
                    activeAccount = activeAccount!!,
                    httpClient = httpClient,
                    accountDao = db.accountDao(),
                    storage = storage,
                    deviceId = params.deviceId,
                    publishFn = { res -> flushResults(res) }
            )
            is GeneralRequest.LinkData -> LinkDataWorker(
                    activeAccount = activeAccount!!,
                    storage = storage,
                    authorizerId = params.authorizerId,
                    db = db,
                    signalClient = signalClient,
                    dataAddress = params.dataAddress,
                    key = params.key,
                    publishFn = { res -> flushResults(res) }
            )
            is GeneralRequest.LinkDataReady -> LinkDataReadyWorker(
                    activeAccount = activeAccount!!,
                    httpClient = httpClient,
                    publishFn = { res -> flushResults(res) }
            )
            is GeneralRequest.SyncStatus -> SyncStatusWorker(
                    activeAccount = activeAccount!!,
                    storage = storage,
                    accountDao = db.accountDao(),
                    httpClient = httpClient,
                    publishFn = { res -> flushResults(res) }
            )
            is GeneralRequest.SyncAccept -> SyncAuthAcceptWorker(
                    activeAccount = activeAccount!!,
                    httpClient = httpClient,
                    storage = storage,
                    accountDao = db.accountDao(),
                    trustedDeviceInfo = params.trustedDeviceInfo,
                    publishFn = { res -> flushResults(res) }
            )
            is GeneralRequest.SyncDenied -> SyncAuthDenyWorker(
                    activeAccount = activeAccount!!,
                    accountDao = db.accountDao(),
                    trustedDeviceInfo = params.trustedDeviceInfo,
                    storage = storage,
                    httpClient = httpClient,
                    publishFn = { res -> flushResults(res) }
            )
        }
    }
}