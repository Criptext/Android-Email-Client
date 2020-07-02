package com.criptext.mail.utils.generaldatasource.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.utils.generaldatasource.workers.*
import java.io.File

class GeneralDataSource(override val runner: WorkRunner,
                        private val filesDir: File,
                        private val signalClient: SignalClient?,
                        private val eventLocalDB: EventLocalDB,
                        private val db : AppDatabase,
                        private val storage: KeyValueStorage,
                        var activeAccount: ActiveAccount?,
                        private val httpClient: HttpClient
): BackgroundWorkManager<GeneralRequest, GeneralResult>() {

    override fun createWorkerFromParams(params: GeneralRequest, flushResults: (GeneralResult) -> Unit): BackgroundWorker<*> {
        return when (params) {
            is GeneralRequest.DeviceRemoved -> DeviceRemovedWorker(
                    letAPIKnow = params.letAPIKnow,
                    activeAccount = activeAccount ?: ActiveAccount.loadFromStorage(storage)!!,
                    httpClient = httpClient, filesDir = filesDir,
                    db = db, storage = storage, publishFn = flushResults
            )
            is GeneralRequest.ConfirmPassword -> ConfirmPasswordWorker(
                    activeAccount = activeAccount!!, httpClient = httpClient, storage = storage,
                    accountDao = db.accountDao(),
                    password = params.password, publishFn = flushResults
            )
            is GeneralRequest.ResetPassword -> ForgotPasswordWorker(
                    recipientId = params.recipientId,
                    domain = params.domain,
                    httpClient = httpClient,
                    publishFn = { res -> flushResults(res) }
            )
            is GeneralRequest.BackgroundAccountsUpdateMailbox -> BackgroundAccountsUpdateMailboxWorker(
                    db = db,
                    dbEvents = eventLocalDB,
                    httpClient = httpClient,
                    accounts = params.accounts,
                    label = params.label,
                    storage = storage,
                    accountDao = db.accountDao(),
                    pendingEventDao = db.pendingEventDao(),
                    publishFn = { res -> flushResults(res) })
            is GeneralRequest.ActiveAccountUpdateMailbox -> ActiveAccountUpdateMailboxWorker(
                    db = db,
                    dbEvents = eventLocalDB,
                    httpClient = httpClient,
                    account = activeAccount!!,
                    label = params.label,
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
                    filesDir = filesDir,
                    db = db,
                    recipientId = params.recipientId,
                    domain = params.domain,
                    storage = storage,
                    publishFn = { res -> flushResults(res)}
            )
            is GeneralRequest.PostUserData -> PostUserWorker(
                    db = db,
                    httpClient = httpClient,
                    activeAccount = params.activeAccount,
                    randomId = params.randomId,
                    filePath = params.filePath,
                    deviceId = params.deviceID,
                    fileKey = params.key,
                    keyBundle = params.keyBundle,
                    accountDao = db.accountDao(),
                    storage = storage,
                    publishFn = { res -> flushResults(res)}
            )
            is GeneralRequest.TotalUnreadEmails -> GetTotalUnreadMailsByLabelWorker(
                    activeAccount = activeAccount!!,
                    emailDao = db.emailDao(),
                    db = MailboxLocalDB.Default(db, filesDir),
                    currentLabel = params.currentLabel,
                    publishFn = { res -> flushResults(res)}
            )
            is GeneralRequest.SyncPhonebook -> SyncPhonebookWorker(
                    activeAccount = activeAccount!!,
                    contactDao = db.contactDao(),
                    contentResolver = params.contentResolver,
                    publishFn = { res -> flushResults(res)}
            )
            is GeneralRequest.Logout -> LogoutWorker(
                    shouldDeleteAllData = params.shouldDeleteAllData,
                    letAPIKnow = params.letAPIKnow,
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
                    filesDir = filesDir,
                    db = db,
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
            is GeneralRequest.ResendEmail -> ResendEmailWorker(
                    filesDir = filesDir,
                    accountDao = db.accountDao(),
                    storage = storage,
                    rawSessionDao = db.rawSessionDao(),
                    appDB = db,
                    db = MailboxLocalDB.Default(db, filesDir),
                    httpClient = httpClient,
                    activeAccount = activeAccount!!,
                    emailId = params.emailId,
                    position = params.position,
                    publishFn = { result ->
                        flushResults(result)
                    }
            )
            is GeneralRequest.ChangeContactName -> ChangeContactNameWorker(
                    fullName = params.fullName,
                    recipientId = params.recipientId,
                    domain = params.domain,
                    db = db,
                    activeAccount = activeAccount!!,
                    httpClient = httpClient,
                    storage = storage,
                    publishFn = { res -> flushResults(res) }
            )
            is GeneralRequest.GetRemoteFile -> GetRemoteFileWorker(
                    uris = params.uris,
                    contentResolver = params.contentResolver,
                    publishFn = { res -> flushResults(res) }
            )
            is GeneralRequest.Set2FA -> TwoFAWorker(
                    storage = storage,
                    accountDao = db.accountDao(),
                    activeAccount = activeAccount!!,
                    twoFA = params.twoFA,
                    httpClient = httpClient,
                    publishFn = { res -> flushResults(res) }
            )
            is GeneralRequest.ChangeToNextAccount -> ChangeToNextAccountWorker(
                    storage = storage,
                    db = MailboxLocalDB.Default(db, filesDir),
                    publishFn = { res -> flushResults(res) }
            )
            is GeneralRequest.GetUserSettings -> GetUserSettingsWorker(
                    storage = storage,
                    accountDao = db.accountDao(),
                    activeAccount = activeAccount!!,
                    httpClient = httpClient,
                    publishFn = { res -> flushResults(res) }
            )
            is GeneralRequest.SyncCancel -> SyncCancelWorker(
                    activeAccount = activeAccount!!,
                    httpClient = httpClient,
                    publishFn = { res -> flushResults(res) }
            )
            is GeneralRequest.LinkCancel -> LinkCancelWorker(
                    username = params.recipientId,
                    domain = params.domain,
                    jwt = params.jwt,
                    deviceId = params.deviceId,
                    httpClient = httpClient,
                    publishFn = { res -> flushResults(res) }
            )
            is GeneralRequest.RestoreMailbox -> RestoreMailboxWorker(
                    activeAccount = activeAccount!!,
                    filePath = params.filePath,
                    db = db,
                    filesDir = filesDir,
                    passphrase = params.passphrase,
                    storage = storage,
                    isLocal = params.isLocal,
                    publishFn = { result ->
                        flushResults(result)
                    }
            )
            is GeneralRequest.Report -> ReportWorker(
                    activeAccount = activeAccount!!,
                    httpClient = httpClient,
                    email = params.emails,
                    type = params.type,
                    publishFn = { result ->
                        flushResults(result)
                    }
            )
            is GeneralRequest.UserEvent -> UserEventWorker(
                    activeAccount = activeAccount!!,
                    httpClient = httpClient,
                    event = params.event,
                    publishFn = { result ->
                        flushResults(result)
                    }
            )
            is GeneralRequest.GetEmailPreview -> GetEmailPreviewWorker(
                    activityMessage = params.activityMessage,
                    threadId = params.threadId,
                    mailboxLocalDB = MailboxLocalDB.Default(db, filesDir),
                    userEmail = params.userEmail,
                    doReply = params.doReply,
                    activeAccount = activeAccount!!,
                    publishFn = { result ->
                        flushResults(result)
                    })
            is GeneralRequest.SetActiveAccountFromPush -> SetActiveAccountFromPushWorker(
                    recipientId = params.recipientId,
                    domain = params.domain,
                    extras = params.extras,
                    db = MailboxLocalDB.Default(db, filesDir),
                    storage = storage,
                    publishFn = { result ->
                        flushResults(result)
                    }
            )
            is GeneralRequest.UpdateLocalDomainAndAliasData -> UpdateLocalDomainAndAliasDataWorker(
                    activeAccount = activeAccount!!,
                    aliasDao = db.aliasDao(),
                    customDomainDao = db.customDomainDao(),
                    aliasData = params.aliases,
                    customDomains = params.customDomains,
                    storage = storage,
                    accountDao = db.accountDao(),
                    publishFn = { result ->
                        flushResults(result)
                    }
            )
            is GeneralRequest.ChangeBlockRemoteContentSetting -> ChangeBlockRemoteContentSettingWorker(
                    activeAccount = activeAccount!!,
                    accountDao = db.accountDao(),
                    httpClient = httpClient,
                    storage = storage,
                    newBlockRemoteContentSetting = params.newBlockRemoteContent,
                    publishFn = { result ->
                        flushResults(result)
                    }
            )
            is GeneralRequest.ResendConfirmationLink -> ResendRecoveryEmailLinkWorker(
                    accountDao = db.accountDao(),
                    storage = storage,
                    activeAccount = activeAccount!!,
                    httpClient = httpClient,
                    publishFn = flushResults
            )
        }
    }
}