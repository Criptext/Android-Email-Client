package com.criptext.mail.scenes.restorebackup.workers

import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.SearchLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.mailbox.data.EmailThread
import com.criptext.mail.scenes.mailbox.data.LoadParams
import com.criptext.mail.scenes.restorebackup.data.RestoreBackupRequest
import com.criptext.mail.scenes.restorebackup.data.RestoreBackupResult
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.google.api.services.drive.Drive

class CheckForBackupWorker(
        private val mDriveServiceHelper: Drive,
        private val activeAccount: ActiveAccount,
        override val publishFn: (
                RestoreBackupResult.CheckForBackup) -> Unit
): BackgroundWorker<RestoreBackupResult.CheckForBackup> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): RestoreBackupResult.CheckForBackup {
        return RestoreBackupResult.CheckForBackup.Failure(UIMessage(resId = R.string.failed_searching_emails))
    }

    override fun work(reporter: ProgressReporter<RestoreBackupResult.CheckForBackup>): RestoreBackupResult.CheckForBackup? {
        val operation = Result.of {
            val folder = mDriveServiceHelper.files().list().setQ("name='${activeAccount.userEmail}'").execute()
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
                    val isEncrypted = driveFile.fileExtension == "enc"
                    Triple(driveFile.getSize() / (1024 * 1024), driveFile.modifiedTime.value, isEncrypted)
                }
            }
        }


        return when(operation){
            is Result.Success -> RestoreBackupResult.CheckForBackup.Success(
                    true,
                    fileSize = operation.value.first,
                    lastModified = operation.value.second,
                    isEncrypted = operation.value.third
            )
            is Result.Failure -> catchException(operation.error)
        }
    }

    override fun cancel() {
    }

}