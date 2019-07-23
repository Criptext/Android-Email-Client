package com.criptext.mail.scenes.settings.cloudbackup.data

import android.content.ContentResolver
import android.net.Uri
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener
import com.google.api.services.drive.Drive

sealed class CloudBackupRequest{
    data class LoadCloudBackupData(val mDriveServiceHelper: Drive?): CloudBackupRequest()
    data class SetCloudBackupActive(val cloudBackupData: CloudBackupData): CloudBackupRequest()
    data class UploadBackupToDrive(val filePath: String, val mDriveServiceHelper: Drive,
                                   val progressListener: MediaHttpUploaderProgressListener?): CloudBackupRequest()
    data class DataFileCreation(val passphrase: String?, val isFromJob: Boolean = false, val isLocal: Boolean = false): CloudBackupRequest()
    data class DeleteFileInDrive(val mDriveServiceHelper: Drive, val fileId: List<String>): CloudBackupRequest()
    data class SaveFileInLocalStorage(val contentResolver: ContentResolver, val fileUri: Uri, val filePath: String): CloudBackupRequest()
}