package com.criptext.mail.scenes.emaildetail.workers

import android.accounts.NetworkErrorException
import android.util.Log
import com.criptext.mail.R
import com.criptext.mail.aes.AESUtil
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.composer.data.FileServiceAPIClient
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.utils.ServerErrorCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.file.AndroidFs
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.nio.ByteBuffer

class DownloadAttachmentWorker(private val fileSize: Long,
                               private val fileName: String,
                               private val fileToken: String,
                               private val emailId: Long,
                               private val fileKey: String?,
                               private val downloadPath: String,
                               httpClient: HttpClient,
                               activeAccount: ActiveAccount,
                             override val publishFn: (EmailDetailResult.DownloadFile) -> Unit)
    : BackgroundWorker<EmailDetailResult.DownloadFile> {

    override val canBeParallelized = false
    var filepath = ""

    private val fileServiceAPIClient = FileServiceAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): EmailDetailResult.DownloadFile =
        if(ex is ServerErrorException) {
            when {
                ex.errorCode == ServerErrorCodes.Unauthorized ->
                    EmailDetailResult.DownloadFile.Unauthorized(UIMessage(R.string.device_removed_remotely_exception))
                ex.errorCode == ServerErrorCodes.Forbidden ->
                    EmailDetailResult.DownloadFile.Forbidden()
                else -> EmailDetailResult.DownloadFile.Failure(fileToken, createErrorMessage(ex))
            }
        }
        else EmailDetailResult.DownloadFile.Failure(fileToken, createErrorMessage(ex))



    private fun downloadFileMetadata(fileToken: String): Result<String, Exception> =
        Result.of {
            fileServiceAPIClient.getFileMetadata(fileToken)
        }

    private val getMetaDataFromJSONResponse: (String) -> Result<FileMetadata, Exception> = { stringResponse ->
        Result.of {
            FileMetadata.fromJSON(stringResponse)
        }
    }

    private fun downloadFile(reporter: ProgressReporter<EmailDetailResult.DownloadFile>)
            : (FileMetadata) -> Result<Unit, Exception> = { fileMetadata ->
        Result.of {
            val file = File(downloadPath, fileMetadata.name)
            val fileStream = FileOutputStream(file)
            val onNewChunkDownload: (Int) -> Unit = { index ->
                reporter.report( EmailDetailResult.DownloadFile.Progress(emailId, fileMetadata.fileToken,
                        index * 100 / fileMetadata.chunks))

                val data = if(fileKey != null)
                                        AESUtil(fileKey).decrypt(fileServiceAPIClient
                                                .downloadChunk(fileMetadata.fileToken, index + 1))
                                    else
                                        fileServiceAPIClient.downloadChunk(fileMetadata.fileToken, index + 1)

                val channel = fileStream.channel
                channel.position(index * fileMetadata.chunkSize)
                channel.write(ByteBuffer.wrap(data))
                if(index == fileMetadata.chunks - 1){
                    fileStream.close()
                    moveFileToDownloads(file, fileMetadata)
                }
            }
            iterateChunks(fileMetadata, onNewChunkDownload)
        }
    }

    private fun moveFileToDownloads(file: File, fileMetadata: FileMetadata){
        val downloadFile = AndroidFs.getFileFromDownloadsDir(fileMetadata.name)
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

    private fun iterateChunks(fileMetadata: FileMetadata, onNewChunkDownload: (Int) -> Unit){
        var index = 0
        while(index < fileMetadata.chunks){
            onNewChunkDownload(index)
            index++
        }
    }

    override fun work(reporter: ProgressReporter<EmailDetailResult.DownloadFile>): EmailDetailResult.DownloadFile? {
        if(AndroidFs.fileExistsInDownloadsDir(fileName, fileSize)){
            filepath = AndroidFs.getFileFromDownloadsDir(fileName).absolutePath
            return EmailDetailResult.DownloadFile.Success(emailId, fileToken, filepath)
        }
        val result = downloadFileMetadata(fileToken)
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
                .flatMap(getMetaDataFromJSONResponse)
                .flatMap(downloadFile(reporter))

        return when (result) {
            is Result.Success -> EmailDetailResult.DownloadFile.Success(emailId, fileToken, filepath)
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