package com.criptext.mail.scenes.settings.cloudbackup.data

import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.cloudbackup.workers.*
import java.io.File

class CloudBackupDataSource(
        var activeAccount: ActiveAccount,
        private val filesDir: File,
        private val storage: KeyValueStorage,
        private val db: AppDatabase,
        override val runner: WorkRunner)
    : BackgroundWorkManager<CloudBackupRequest, CloudBackupResult>(){

    override fun createWorkerFromParams(params: CloudBackupRequest,
                                        flushResults: (CloudBackupResult) -> Unit): BackgroundWorker<*> {

        return when(params){
            is CloudBackupRequest.LoadCloudBackupData -> LoadCloudBackupDataWorker(
                    activeAccount = activeAccount,
                    accountDao = db.accountDao(),
                    storage = storage,
                    mDriveServiceHelper = params.mDriveServiceHelper,
                    publishFn = { res -> flushResults(res) }
            )
            is CloudBackupRequest.SetCloudBackupActive -> SetCloudBackupActiveWorker(
                    activeAccount = activeAccount,
                    cloudBackupData = params.cloudBackupData,
                    accountDao = db.accountDao(),
                    publishFn = { res -> flushResults(res) }
            )
            is CloudBackupRequest.UploadBackupToDrive -> UploadBackupToDriveWorker(
                    activeAccount = activeAccount,
                    filePath = params.filePath,
                    mDriveServiceHelper = params.mDriveServiceHelper,
                    accountDao = db.accountDao(),
                    progressListener = params.progressListener,
                    storage = storage,
                    publishFn = { res -> flushResults(res) }
            )
            is CloudBackupRequest.DataFileCreation -> DataFileCreationWorker(
                    passphrase = params.passphrase,
                    isFromJob = params.isFromJob,
                    isLocal = params.isLocal,
                    activeAccount = activeAccount,
                    db = db,
                    filesDir = filesDir,
                    storage = storage,
                    publishFn = { res -> flushResults(res) }
            )
            is CloudBackupRequest.DeleteFileInDrive -> DeleteOldBackupWorker(
                    fileId = params.fileId,
                    mDriveServiceHelper = params.mDriveServiceHelper,
                    publishFn = { res -> flushResults(res) }
            )
            is CloudBackupRequest.SaveFileInLocalStorage -> SaveFileInLocalStorageWorker(
                    filePath = params.filePath,
                    uri = params.fileUri,
                    contentResolver = params.contentResolver,
                    publishFn = { res -> flushResults(res) }
            )
        }
    }
}