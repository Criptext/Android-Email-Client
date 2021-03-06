package com.criptext.mail.scenes.settings.cloudbackup.data

import com.criptext.mail.utils.UIMessage
import java.util.*

sealed class CloudBackupResult{

    sealed class LoadCloudBakcupData : CloudBackupResult() {
        data class Success(val cloudBackupData: CloudBackupData): LoadCloudBakcupData()
        data class Failure(val message: UIMessage,
                           val exception: Exception?,
                           val cloudBackupData: CloudBackupData): LoadCloudBakcupData()
    }

    sealed class SetCloudBackupActive : CloudBackupResult() {
        data class Success(val cloudBackupData: CloudBackupData): SetCloudBackupActive()
        data class Failure(val message: UIMessage,
                           val exception: Exception?,
                           val cloudBackupData: CloudBackupData): SetCloudBackupActive()
    }

    sealed class UploadBackupToDrive : CloudBackupResult() {
        data class Success(val fileLength: Long, val lastModified: Date, val hasOldFile: Boolean, val oldFileIds: List<String>): UploadBackupToDrive()
        data class Progress(val progress: Int): UploadBackupToDrive()
        data class Failure(val message: UIMessage,
                           val exception: Exception?): UploadBackupToDrive()
    }

    sealed class DataFileCreation: CloudBackupResult() {
        data class Success(val filePath: String, val isLocal: Boolean, val isEncrypted: Boolean): DataFileCreation()
        data class Progress(val progress: Int): DataFileCreation()
        data class Failure(val message: UIMessage): DataFileCreation()
    }

    sealed class DeleteFileInDrive : CloudBackupResult() {
        class Success: DeleteFileInDrive()
        data class Failure(val message: UIMessage,
                           val exception: Exception?): DeleteFileInDrive()
    }

    sealed class SaveFileInLocalStorage : CloudBackupResult() {
        class Success: SaveFileInLocalStorage()
        data class Failure(val message: UIMessage): SaveFileInLocalStorage()
    }

}