package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.aes.AESUtil
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.dao.*
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.utils.Encoding
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import java.io.File
import java.io.IOException
import java.io.FileInputStream
import java.util.zip.GZIPOutputStream
import java.io.FileOutputStream




class DataFileCreationWorker(
        private val filesDir: File,
        private val db: AppDatabase,
        private val recipientId: String,
        private val domain: String,
        override val publishFn: (
                GeneralResult.DataFileCreation) -> Unit)
    : BackgroundWorker<GeneralResult.DataFileCreation> {

    override val canBeParallelized = true

    private lateinit var filePath: String

    override fun catchException(ex: Exception): GeneralResult.DataFileCreation {
        val message = UIMessage(resId = R.string.failed_to_create_link_device_file)
        return GeneralResult.DataFileCreation.Failure(
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

    override fun work(reporter: ProgressReporter<GeneralResult.DataFileCreation>)
            : GeneralResult.DataFileCreation? {
        val account = db.accountDao().getAccount(recipientId, domain) ?: return GeneralResult.DataFileCreation.Failure(UIMessage(resId = R.string.no_account_to_sync))
        val dataWriter = UserDataWriter(db, filesDir)
        reporter.report(GeneralResult.DataFileCreation.Progress(UIMessage(R.string.preparing_mailbox), 40))
        val getFileResult = dataWriter.createFile(account)
        return if(getFileResult != null){
            reporter.report(GeneralResult.DataFileCreation.Progress(UIMessage(R.string.preparing_mailbox), 45))
            val path = compress(getFileResult)
            if(path == null){
                GeneralResult.DataFileCreation.Failure(UIMessage(resId = R.string.failed_to_create_link_device_file))
            }else{
                filePath = path
                reporter.report(GeneralResult.DataFileCreation.Progress(UIMessage(R.string.preparing_mailbox), 55))
                val fileByChunks = AESUtil.encryptFileByChunks(File(filePath))
                GeneralResult.DataFileCreation.Success(fileByChunks.first, fileByChunks.second)
            }
        }else
            GeneralResult.DataFileCreation.Failure(UIMessage(resId = R.string.failed_to_create_link_device_file))

    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }
}