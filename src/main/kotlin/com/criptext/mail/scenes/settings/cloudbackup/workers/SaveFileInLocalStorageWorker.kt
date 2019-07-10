package com.criptext.mail.scenes.settings.cloudbackup.workers

import android.content.ContentResolver
import android.net.Uri
import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.scenes.settings.cloudbackup.data.CloudBackupResult
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import java.io.*
import java.util.zip.GZIPOutputStream


class SaveFileInLocalStorageWorker(
        private val filePath: String,
        private val uri: Uri,
        private var contentResolver: ContentResolver,
        override val publishFn: (
                CloudBackupResult.SaveFileInLocalStorage) -> Unit)
    : BackgroundWorker<CloudBackupResult.SaveFileInLocalStorage> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): CloudBackupResult.SaveFileInLocalStorage {
        val message = UIMessage(resId = R.string.failed_to_create_link_device_file)
        return CloudBackupResult.SaveFileInLocalStorage.Failure(
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

    override fun work(reporter: ProgressReporter<CloudBackupResult.SaveFileInLocalStorage>)
            : CloudBackupResult.SaveFileInLocalStorage? {
        val operation = Result.of {
            val outputStream: OutputStream = contentResolver.openOutputStream(uri) ?: throw Exception()

            val fileStream = FileInputStream(File(filePath))

            fileStream.use { input ->
                outputStream.use { fileOut ->
                    input.copyTo(fileOut)
                }
            }

            fileStream.close()
            outputStream.close()

        }

        return when(operation) {
            is Result.Success -> CloudBackupResult.SaveFileInLocalStorage.Success()
            is Result.Failure -> catchException(operation.error)
        }


    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }
}