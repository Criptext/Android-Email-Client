package com.criptext.mail.scenes.restorebackup.data

import com.criptext.mail.utils.UIMessage

sealed class RestoreBackupResult{

    sealed class CheckForBackup : RestoreBackupResult() {
        data class Success(
                val hasBackup: Boolean,
                val fileSize: Long,
                val lastModified: Long,
                val isEncrypted: Boolean)
            : CheckForBackup()
        data class Failure(val message: UIMessage) : CheckForBackup()
    }

    sealed class DownloadBackup : RestoreBackupResult() {
        data class Success(val filePath: String): DownloadBackup()
        data class Failure(val message: UIMessage) : DownloadBackup()
    }

    sealed class RestoreMailbox : RestoreBackupResult() {
        class Success: RestoreMailbox()
        data class Progress(val progress: Int): RestoreMailbox()
        data class Failure(val message: UIMessage) : RestoreMailbox()
    }
}