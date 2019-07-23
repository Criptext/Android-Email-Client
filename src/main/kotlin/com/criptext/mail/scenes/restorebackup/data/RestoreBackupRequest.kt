package com.criptext.mail.scenes.restorebackup.data

import com.criptext.mail.scenes.restorebackup.RestoreBackupController
import com.google.api.services.drive.Drive

sealed class RestoreBackupRequest{
    data class CheckForBackup(val mDriveService: Drive): RestoreBackupRequest()
    data class DownloadBackup(val mDriveService: Drive, val progressListener: RestoreBackupController.RestoreProgressListener): RestoreBackupRequest()
}