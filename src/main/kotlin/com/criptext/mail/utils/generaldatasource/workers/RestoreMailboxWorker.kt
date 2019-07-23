package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.aes.AESUtil
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.restorebackup.data.RestoreBackupResult
import com.criptext.mail.utils.EmailUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.exceptions.SyncFileException
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.ZipException
import javax.crypto.BadPaddingException

class RestoreMailboxWorker(private val filesDir: File,
                           private var activeAccount: ActiveAccount,
                           private val filePath: String,
                           private val passphrase: String?,
                           private val isLocal: Boolean,
                           private val db: AppDatabase,
                           override val publishFn: (GeneralResult) -> Unit)
    : BackgroundWorker<GeneralResult.RestoreMailbox> {

    private val dataWriter = UserDataWriter(db, filesDir)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): GeneralResult.RestoreMailbox {
        if(ex is SyncFileException || ex is ZipException){
            return GeneralResult.RestoreMailbox.SyncError(createErrorMessage(ex))
        }
        return GeneralResult.RestoreMailbox.Failure(createErrorMessage(ex))
    }

    private fun decompress(sourceFile: String): String {
        val targetFile = createTempFile()

        val fis = FileInputStream(sourceFile)
        val gzis = GZIPInputStream(fis)
        val buffer = ByteArray(1024)
        val fos = FileOutputStream(targetFile)
        var length = gzis.read(buffer)
        while (length > 0) {
            fos.write(buffer, 0, length)
            length = gzis.read(buffer)
        }
        fos.close()
        gzis.close()
        fis.close()
        return targetFile.absolutePath
    }

    override fun work(reporter: ProgressReporter<GeneralResult.RestoreMailbox>): GeneralResult.RestoreMailbox? {
        val result =  Result.of {
            if(!isLocal) reporter.report(GeneralResult.RestoreMailbox.Progress(20))
            val path = if(passphrase != null){
                AESUtil.decryptFileByChunksWithCustomPassword(File(filePath), passphrase)
            } else {
                filePath
            }
            if(!isLocal){
                reporter.report(GeneralResult.RestoreMailbox.Progress(90))
            }
            decompress(path)
        }.flatMap {
            Result.of {
                val decompressedFile = File(it)
                deleteLocalData()
                dataWriter.createDBFromFile(decompressedFile)
                if(!isLocal) reporter.report(GeneralResult.RestoreMailbox.Progress(100))
            }
        }


        return when (result) {
            is Result.Success ->{
                GeneralResult.RestoreMailbox.Success()
            }
            is Result.Failure -> {
                result.error.printStackTrace()
                catchException(result.error)
            }
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    private fun deleteLocalData(){
        db.accountContactDao().nukeTable(activeAccount.id)
        db.pendingEventDao().nukeTable(activeAccount.id)
        db.labelDao().nukeTable(activeAccount.id)
        db.emailDao().nukeTable(activeAccount.id)
        EmailUtils.deleteEmailsInFileSystem(filesDir, activeAccount.recipientId, activeAccount.domain)
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when(ex){
            is SyncFileException.OutdatedException -> {
                UIMessage(resId = R.string.restore_backup_version_incompatible)
            }
            is SyncFileException.UserNotValidException -> {
                UIMessage(resId = R.string.restore_backup_account_incompatible)
            }
            else -> UIMessage(resId = R.string.restore_backup_fail_message)
        }
    }

}