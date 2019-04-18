package com.criptext.mail.scenes.restorebackup.workers

import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.restorebackup.RestoreBackupController
import com.criptext.mail.scenes.restorebackup.data.RestoreBackupResult
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.google.api.client.googleapis.media.MediaHttpDownloader
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener
import com.google.api.services.drive.Drive
import java.io.File
import java.io.FileOutputStream


class DownloadFileWorker(
        private val mDriveServiceHelper: Drive,
        private val activeAccount: ActiveAccount,
        private val progressListener: RestoreBackupController.RestoreProgressListener,
        override val publishFn: (
                RestoreBackupResult.DownloadBackup) -> Unit
): BackgroundWorker<RestoreBackupResult.DownloadBackup> {

    private var filePath = ""

    override val canBeParallelized = true

    override fun catchException(ex: Exception): RestoreBackupResult.DownloadBackup {
        return RestoreBackupResult.DownloadBackup.Failure(UIMessage(resId = R.string.failed_searching_emails))
    }

    override fun work(reporter: ProgressReporter<RestoreBackupResult.DownloadBackup>): RestoreBackupResult.DownloadBackup? {
        val operation = Result.of {
            val parentFolder = mDriveServiceHelper.files().list().setQ("name='Criptext Backups'").execute()
            if(parentFolder.files.isEmpty())
                throw Exception()
            val folder = mDriveServiceHelper.files().list().setQ("name='${activeAccount.userEmail}' and ('${parentFolder.files.first().id}' in parents)").execute()
            if(folder.files.isEmpty()){
                throw Exception()
            } else {
                val file = mDriveServiceHelper.files().list()
                        .setQ("name contains 'Mailbox Backup' and ('${folder.files.first().id}' in parents) and trashed=false")
                        .setFields("*")
                        .execute()
                if(file.files.isEmpty())
                    throw Exception()
                else {
                    val driveFile = file.files.first()
                    val tmpFile = File.createTempFile("temp", driveFile.fileExtension)
                    filePath = tmpFile.absolutePath
                    val out = FileOutputStream(tmpFile.absolutePath)

                    val request = mDriveServiceHelper.files().get(driveFile.id)
                    request.mediaHttpDownloader.progressListener = progressListener
                    request.executeMediaAndDownloadTo(out)
                }
            }
        }

        return when(operation){
            is Result.Success -> RestoreBackupResult.DownloadBackup.Success(filePath)
            is Result.Failure -> catchException(operation.error)
        }
    }

    override fun cancel() {
    }

}