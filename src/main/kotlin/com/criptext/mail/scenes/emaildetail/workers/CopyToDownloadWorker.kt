package com.criptext.mail.scenes.emaildetail.workers

import android.accounts.NetworkErrorException
import com.criptext.mail.R
import com.criptext.mail.aes.AESUtil
import com.criptext.mail.api.CriptextAPIClient
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.EmailDetailLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.composer.data.FileServiceAPIClient
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.file.AndroidFs
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.nio.ByteBuffer

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
        when (ex) { // these are not the real errors TODO fix!
            is JSONException -> UIMessage(resId = R.string.json_error_exception)
            is ServerErrorException -> UIMessage(resId = R.string.server_error_exception)
            is NetworkErrorException -> UIMessage(resId = R.string.network_error_exception)
            else -> UIMessage(resId = R.string.error_downloading_file)
        }
    }


    data class FileMetadata(val fileToken: String, val name: String, val chunkSize: Long, val chunks: Int){
        companion object {
            fun fromJSON(metadataJsonString: String): FileMetadata {
                val jsonObject = JSONObject(metadataJsonString)
                val fileObject = jsonObject.getJSONObject("file")
                val token = fileObject.getString("token")
                val name = fileObject.getString("name")
                val chunkSize = fileObject.getLong("chunk_size")
                val chunks = fileObject.getInt("chunks")
                return FileMetadata(token, name, chunkSize, chunks)
            }
        }
    }
}