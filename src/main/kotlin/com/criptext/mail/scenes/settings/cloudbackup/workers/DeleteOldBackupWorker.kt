package com.criptext.mail.scenes.settings.cloudbackup.workers

import com.criptext.mail.R
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.cloudbackup.CloudBackupController
import com.criptext.mail.scenes.settings.cloudbackup.data.CloudBackupResult
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.google.api.client.googleapis.media.MediaHttpUploader
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener
import com.google.api.client.http.InputStreamContent
import com.google.api.services.drive.Drive
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*


class DeleteOldBackupWorker(private val fileId: String,
                            private val mDriveServiceHelper: Drive,
                            override val publishFn: (CloudBackupResult) -> Unit)
    : BackgroundWorker<CloudBackupResult.DeleteFileInDrive> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): CloudBackupResult.DeleteFileInDrive {
        return if(ex is ServerErrorException) {
            when(ex.errorCode) {
                ServerCodes.MethodNotAllowed -> CloudBackupResult.DeleteFileInDrive.Failure(UIMessage(R.string.message_warning_two_fa), ex)
                else -> CloudBackupResult.DeleteFileInDrive.Failure(UIMessage(R.string.server_error_exception), ex)
            }
        }else {
            CloudBackupResult.DeleteFileInDrive.Failure(UIMessage(R.string.server_error_exception), ex)
        }
    }

    override fun work(reporter: ProgressReporter<CloudBackupResult.DeleteFileInDrive>): CloudBackupResult.DeleteFileInDrive? {
        val result =  Result.of {
            mDriveServiceHelper.files().delete(fileId).execute()
        }

        return when (result) {
            is Result.Success -> CloudBackupResult.DeleteFileInDrive.Success()

            is Result.Failure -> catchException(result.error)
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }
}