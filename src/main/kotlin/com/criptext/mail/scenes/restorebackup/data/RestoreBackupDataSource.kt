package com.criptext.mail.scenes.restorebackup.data

import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.restorebackup.workers.CheckForBackupWorker
import com.criptext.mail.scenes.restorebackup.workers.DownloadFileWorker
import com.criptext.mail.scenes.restorebackup.workers.RestoreMailboxWorker
import java.io.File

class RestoreBackupDataSource(
        private val storage: KeyValueStorage,
        private val activeAccount: ActiveAccount,
        private val filesDir: File,
        private val db: AppDatabase,
        override val runner: WorkRunner)
    : BackgroundWorkManager<RestoreBackupRequest, RestoreBackupResult>(){

    override fun createWorkerFromParams(params: RestoreBackupRequest, flushResults: (RestoreBackupResult) -> Unit): BackgroundWorker<*> {
        return when(params){
            is RestoreBackupRequest.CheckForBackup -> CheckForBackupWorker(
                    activeAccount = activeAccount,
                    mDriveServiceHelper = params.mDriveService,
                    publishFn = { result ->
                        flushResults(result)
                    }
            )
            is RestoreBackupRequest.DownloadBackup -> DownloadFileWorker(
                    activeAccount = activeAccount,
                    mDriveServiceHelper = params.mDriveService,
                    progressListener = params.progressListener,
                    publishFn = { result ->
                        flushResults(result)
                    }
            )
            is RestoreBackupRequest.RestoreMailbox -> RestoreMailboxWorker(
                    activeAccount = activeAccount,
                    filePath = params.filePath,
                    db = db,
                    filesDir = filesDir,
                    passphrase = params.passphrase,
                    publishFn = { result ->
                        flushResults(result)
                    }
            )
        }
    }
}