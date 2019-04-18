package com.criptext.mail.scenes.settings.cloudbackup.workers

import com.criptext.mail.R
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.cloudbackup.data.CloudBackupResult
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener
import com.google.api.client.http.InputStreamContent
import com.google.api.services.drive.Drive
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.util.*


class UploadBackupToDriveWorker(val activeAccount: ActiveAccount,
                                private val accountDao: AccountDao,
                                private val filePath: String,
                                private val mDriveServiceHelper: Drive,
                                private val storage: KeyValueStorage,
                                private val progressListener: MediaHttpUploaderProgressListener?,
                                override val publishFn: (CloudBackupResult) -> Unit)
    : BackgroundWorker<CloudBackupResult.UploadBackupToDrive> {

    override val canBeParallelized = true

    var oldFile = com.google.api.services.drive.model.FileList()

    override fun catchException(ex: Exception): CloudBackupResult.UploadBackupToDrive {
        return if(ex is ServerErrorException) {
            when(ex.errorCode) {
                ServerCodes.MethodNotAllowed -> CloudBackupResult.UploadBackupToDrive.Failure(UIMessage(R.string.message_warning_two_fa), ex)
                else -> CloudBackupResult.UploadBackupToDrive.Failure(UIMessage(R.string.server_error_exception), ex)
            }
        }else {
            CloudBackupResult.UploadBackupToDrive.Failure(UIMessage(R.string.server_error_exception), ex)
        }
    }

    override fun work(reporter: ProgressReporter<CloudBackupResult.UploadBackupToDrive>): CloudBackupResult.UploadBackupToDrive? {
        val result =  Result.of {

            val criptextFolder = mDriveServiceHelper.files().list().setQ("name='Criptext Backups'").execute()
            val rootFolder = if(criptextFolder.files.isEmpty()){

                val folderMetadata = com.google.api.services.drive.model.File()
                folderMetadata.name = "Criptext Backups"
                folderMetadata.mimeType = "application/vnd.google-apps.folder"

                mDriveServiceHelper.files().create(folderMetadata)
                        .setFields("id")
                        .execute()
            } else {
                criptextFolder.files.first()
            }

            val folder = mDriveServiceHelper.files().list().setQ("name='${activeAccount.userEmail}' and ('${rootFolder.id}' in parents)").execute()
            val parentFolder = if(folder.files.isEmpty()){

                val folderMetadata = com.google.api.services.drive.model.File()
                folderMetadata.name = activeAccount.userEmail
                folderMetadata.mimeType = "application/vnd.google-apps.folder"
                folderMetadata.parents = listOf(rootFolder.id)

                mDriveServiceHelper.files().create(folderMetadata)
                        .setFields("id")
                        .execute()
            } else {
                folder.files.first()
            }

            oldFile = mDriveServiceHelper.files().list()
                    .setQ("name contains 'Mailbox Backup' and ('${parentFolder.id}' in parents) and trashed=false")
                    .execute()

            val mediaFile = File(filePath)
            val fileMetadata = com.google.api.services.drive.model.File()
            fileMetadata.name = "Mailbox Backup." + mediaFile.extension
            fileMetadata.mimeType = "text/plain"
            fileMetadata.parents = listOf(parentFolder.id)

            val mediaContent = InputStreamContent(fileMetadata.mimeType,
                    BufferedInputStream(FileInputStream(mediaFile)))
            mediaContent.length = mediaFile.length()

            val request = mDriveServiceHelper.files().create(fileMetadata, mediaContent)
            request.mediaHttpUploader.progressListener = progressListener
            request.executeAsInputStream()
            accountDao.updateLastBackupDate(Date())
        }

        return when (result) {
            is Result.Success -> {
                val size = File(filePath).length() / (1024*1024)
                val lastModified = Date()

                storage.putLong(KeyValueStorage.StringKey.LastBackupSize, size)
                storage.putLong(KeyValueStorage.StringKey.LastBackupDate, lastModified.time)

                CloudBackupResult.UploadBackupToDrive.Success(
                    fileLength = size,
                    lastModified = lastModified,
                    hasOldFile = oldFile.files.isNotEmpty(),
                    oldFileId = if(oldFile.files.isNotEmpty()) oldFile.files.first().id else null
                    )
            }
            is Result.Failure -> catchException(result.error)
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }
}