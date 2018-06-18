package com.email.scenes.emaildetail.workers

import android.accounts.NetworkErrorException
import android.util.Log
import com.email.R
import com.email.api.HttpClient
import com.email.api.HttpErrorHandlingHelper
import com.email.api.ServerErrorException
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.scenes.composer.data.FileServiceAPIClient
import com.email.scenes.emaildetail.data.EmailDetailResult
import com.email.utils.UIMessage
import com.email.utils.file.AndroidFs
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer

class DownloadAttachmentWorker(private val fileToken: String,
                               private val dirPath: String,
                             httpClient: HttpClient,
                             fileServiceAuthToken: String,
                             override val publishFn: (EmailDetailResult.DownloadFile) -> Unit)
    : BackgroundWorker<EmailDetailResult.DownloadFile> {

    override val canBeParallelized = false
    var filepath = ""

    val fileServiceAPIClient = FileServiceAPIClient(httpClient, fileServiceAuthToken)

    override fun catchException(ex: Exception): EmailDetailResult.DownloadFile {
        return EmailDetailResult.DownloadFile.Failure(fileToken, createErrorMessage(ex))
    }


    private fun downloadFileMetadata(fileToken: String): Result<String, Exception> =
        Result.of {
            fileServiceAPIClient.getFileMetadata(fileToken)
        }

    private val getMetaDataFromJSONResponse: (String) -> Result<FileMetadata, Exception> = { stringResponse ->
        Result.of {
            val jsonObject = JSONObject(stringResponse)
            val fileObject = jsonObject.getJSONObject("file")
            val token = fileObject.getString("token")
            val name = fileObject.getString("name")
            val chunkSize = fileObject.getLong("chunk_size")
            val chunks = fileObject.getInt("chunks")
            FileMetadata(token, name, chunkSize, chunks)
        }
    }

    private fun downloadFile(reporter: ProgressReporter<EmailDetailResult.DownloadFile>)
            : (FileMetadata) -> Result<Unit, Exception> = { fileMetadata ->
        Result.of {
            val file = AndroidFs.getFileFromDownloadsDir(fileMetadata.name)
            val fileStream = FileOutputStream(file, true)
            filepath = file.absolutePath
            val onNewChunkDownload: (Int) -> Unit = { index ->
                val data = fileServiceAPIClient.downloadChunk(fileMetadata.fileToken, index + 1)
                val channel = fileStream.channel
                channel.position(index * fileMetadata.chunkSize)
                channel.write(ByteBuffer.wrap(data))
                if(index == fileMetadata.chunks - 1){
                    fileStream.close()
                }
            }
            iterateChunks(fileMetadata, onNewChunkDownload)
        }
    }

    private fun iterateChunks(fileMetadata: FileMetadata, onNewChunkDownload: (Int) -> Unit){
        var index = 0
        while(index < fileMetadata.chunks){
            onNewChunkDownload(index)
            index++
        }
    }

    override fun work(reporter: ProgressReporter<EmailDetailResult.DownloadFile>): EmailDetailResult.DownloadFile? {
        val result = downloadFileMetadata(fileToken)
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
                .flatMap(getMetaDataFromJSONResponse)
                .flatMap(downloadFile(reporter))

        return when (result) {
            is Result.Success -> EmailDetailResult.DownloadFile.Success(filepath)
            is Result.Failure -> EmailDetailResult.DownloadFile.Failure(fileToken,
                    createErrorMessage(result.error))
        }
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        ex.printStackTrace()
        when (ex) { // these are not the real errors TODO fix!
            is JSONException -> UIMessage(resId = R.string.json_error_exception)
            is ServerErrorException -> {
                if (ex.errorCode == 400) {
                    UIMessage(resId = R.string.duplicate_name_error_exception)
                } else {
                    UIMessage(resId = R.string.server_error_exception)
                }
            }
            is NetworkErrorException -> UIMessage(resId = R.string.network_error_exception)
            else -> UIMessage(resId = R.string.fail_register_try_again_error_exception)
        }
    }


    inner class FileMetadata(val fileToken: String, val name: String, val chunkSize: Long, val chunks: Int)
}