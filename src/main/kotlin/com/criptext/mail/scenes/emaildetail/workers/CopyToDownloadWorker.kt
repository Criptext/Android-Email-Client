package com.criptext.mail.scenes.emaildetail.workers

import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.file.AndroidFs
import com.github.kittinunf.result.Result
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class CopyToDownloadWorker(private val internalPath: String,
                           override val publishFn: (EmailDetailResult.CopyToDownloads) -> Unit)
    : BackgroundWorker<EmailDetailResult.CopyToDownloads> {

    override val canBeParallelized = true
    var filepath = ""

    override fun catchException(ex: Exception): EmailDetailResult.CopyToDownloads =
        EmailDetailResult.CopyToDownloads.Failure(createErrorMessage(ex))

    private fun moveFileToDownloads(file: File){
        val downloadFile = AndroidFs.getFileFromDownloadsDir(file.name)
        val fileStream = FileInputStream(file)
        val downloadStream = FileOutputStream(downloadFile)

        fileStream.use { input ->
            downloadStream.use { fileOut ->
                input.copyTo(fileOut)
            }
        }

        fileStream.close()
        downloadStream.close()
        file.delete()
        filepath = downloadFile.absolutePath
    }

    override fun work(reporter: ProgressReporter<EmailDetailResult.CopyToDownloads>): EmailDetailResult.CopyToDownloads? {

        val file = File(internalPath)

        if(AndroidFs.fileExistsInDownloadsDir(file.name))
            return EmailDetailResult.CopyToDownloads.Success(UIMessage(R.string.move_to_downloads_exists))

        val result = Result.of {
            moveFileToDownloads(file)
        }


        return when (result) {
            is Result.Success -> EmailDetailResult.CopyToDownloads.Success(UIMessage(R.string.move_to_downloads_success, arrayOf(filepath)))
            is Result.Failure -> catchException(result.error)
        }
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        ex.printStackTrace()
        UIMessage(resId = R.string.copy_to_download_error, args = arrayOf(File(internalPath).name))
    }
}