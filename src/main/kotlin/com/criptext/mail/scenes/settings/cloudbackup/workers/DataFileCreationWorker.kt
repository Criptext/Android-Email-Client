package com.criptext.mail.scenes.settings.cloudbackup.workers

import com.criptext.mail.R
import com.criptext.mail.aes.AESUtil
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.restorebackup.data.RestoreBackupResult
import com.criptext.mail.scenes.settings.cloudbackup.data.CloudBackupResult
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.GZIPOutputStream


class DataFileCreationWorker(
        private val filesDir: File,
        private val db: AppDatabase,
        private var passphrase: String?,
        private val isFromJob: Boolean,
        private val activeAccount: ActiveAccount,
        override val publishFn: (
                CloudBackupResult.DataFileCreation) -> Unit)
    : BackgroundWorker<CloudBackupResult.DataFileCreation> {

    override val canBeParallelized = true

    private lateinit var filePath: String

    override fun catchException(ex: Exception): CloudBackupResult.DataFileCreation {
        val message = UIMessage(resId = R.string.failed_to_create_link_device_file)
        return CloudBackupResult.DataFileCreation.Failure(
                message = message)
    }

    private fun compress(sourceFile: String): String? {
        val targetFile = createTempFile("compressed", ".gz")
        try {
            val fos = FileOutputStream(targetFile)
            val gzos = GZIPOutputStream(fos)
            val buffer = ByteArray(1024)
            val fis = FileInputStream(sourceFile)
            var length= fis.read(buffer)
            while (length > 0) {
                gzos.write(buffer, 0, length)
                length = fis.read(buffer)
            }
            fis.close()
            gzos.finish()
            gzos.close()
            return targetFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun work(reporter: ProgressReporter<CloudBackupResult.DataFileCreation>)
            : CloudBackupResult.DataFileCreation? {
        val dataWriter = UserDataWriter(db, filesDir)
        reporter.report(CloudBackupResult.DataFileCreation.Progress(15))
        val account = db.accountDao().getAccountById(activeAccount.id) ?: return CloudBackupResult.DataFileCreation.Failure(UIMessage(resId = R.string.failed_to_create_link_device_file))
        val getFileResult = dataWriter.createFile(account)
        return if(getFileResult != null){
            reporter.report(CloudBackupResult.DataFileCreation.Progress(35))
            val path = compress(getFileResult)
            if(path == null){
                CloudBackupResult.DataFileCreation.Failure(UIMessage(resId = R.string.failed_to_create_link_device_file))
            }else{
                filePath = path
                reporter.report(CloudBackupResult.DataFileCreation.Progress(55))
                if(isFromJob) {
                    val accountById = db.accountDao().getAccountById(activeAccount.id) ?: throw Exception()
                    passphrase = accountById.backupPassword
                }
                if(passphrase == null)
                    return CloudBackupResult.DataFileCreation.Success(filePath)
                reporter.report(CloudBackupResult.DataFileCreation.Progress(75))
                val encryptedFilePath = AESUtil.encryptFileByChunksWithCustomPassword(File(filePath), passphrase!!)
                reporter.report(CloudBackupResult.DataFileCreation.Progress(95))
                CloudBackupResult.DataFileCreation.Success(encryptedFilePath)
            }
        }else
            CloudBackupResult.DataFileCreation.Failure(UIMessage(resId = R.string.failed_to_create_link_device_file))

    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }
}