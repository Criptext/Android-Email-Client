package com.criptext.mail.scenes.emaildetail.workers

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
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

class DownloadAttachmentWorker(private val fileSize: Long,
                               private val db: EmailDetailLocalDB,
                               private val cid: String?,
                               private val fileName: String,
                               private val fileToken: String,
                               private val emailId: Long,
                               private val fileKey: String?,
                               private val downloadPath: String,
                               private val accountDao: AccountDao,
                               private val storage: KeyValueStorage,
                               httpClient: HttpClient,
                               private val activeAccount: ActiveAccount,
                             override val publishFn: (EmailDetailResult.DownloadFile) -> Unit)
    : BackgroundWorker<EmailDetailResult.DownloadFile> {

    override val canBeParallelized = false
    var filepath = ""

    private lateinit var apiClient: CriptextAPIClient
    private val fileServiceAPIClient = FileServiceAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): EmailDetailResult.DownloadFile =
        if(ex is ServerErrorException) {
            when {
                ex.errorCode == ServerCodes.Unauthorized ->
                    EmailDetailResult.DownloadFile.Unauthorized(UIMessage(R.string.device_removed_remotely_exception))
                ex.errorCode == ServerCodes.Forbidden ->
                    EmailDetailResult.DownloadFile.Forbidden()
                ex.errorCode == ServerCodes.EnterpriseAccountSuspended ->
                    EmailDetailResult.DownloadFile.EnterpriseSuspended()
                else -> EmailDetailResult.DownloadFile.Failure(emailId, fileToken, createErrorMessage(ex))
            }
        }
        else EmailDetailResult.DownloadFile.Failure(emailId, fileToken, createErrorMessage(ex))



    private fun downloadFileMetadata(fileToken: String): Result<String, Exception> =
        Result.of {
            fileServiceAPIClient.getFileMetadata(fileToken).body
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
                if(cid == null || cid == "") {
                    reporter.report(EmailDetailResult.DownloadFile.Progress(emailId, fileMetadata.fileToken,
                            index * 100 / fileMetadata.chunks))
                }

                val data = if(fileKey != null) {
                    AESUtil(fileKey).decrypt(fileServiceAPIClient
                            .downloadChunk(fileMetadata.fileToken, index + 1))
                } else
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
        val downloadFile = if(cid == null || cid == "") AndroidFs.getFileFromDownloadsDir(fileMetadata.name)
        else AndroidFs.getEmailPathFromAppDir(filename = fileMetadata.name, fileDir = db.getInternalFilesDir(),
                recipientId = activeAccount.recipientId, metadataKey = db.getEmailMetadataKeyById(emailId, activeAccount.id))
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
        if(cid == null || cid == ""){
            if(AndroidFs.fileExistsInDownloadsDir(fileName, fileSize)) {
                filepath = AndroidFs.getFileFromDownloadsDir(fileName).absolutePath
                return EmailDetailResult.DownloadFile.Success(emailId, fileToken, filepath, cid)
            }
        }else{
            if(AndroidFs.fileExistsInAppDir(filename = fileName, fileDir = db.getInternalFilesDir(),
                            recipientId = activeAccount.recipientId, metadataKey = db.getEmailMetadataKeyById(emailId, activeAccount.id),
                            fileSize = fileSize)) {
                filepath = AndroidFs.getEmailPathFromAppDir(filename = fileName, fileDir = db.getInternalFilesDir(),
                        recipientId = activeAccount.recipientId, metadataKey = db.getEmailMetadataKeyById(emailId, activeAccount.id)).absolutePath
                return EmailDetailResult.DownloadFile.Success(emailId, fileToken, filepath, cid)
            }
        }
        val result = workOperation(reporter)

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(result)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation(reporter)
        else
            result

        return when (finalResult) {
            is Result.Success -> EmailDetailResult.DownloadFile.Success(emailId, fileToken, filepath, cid)
            is Result.Failure -> catchException(finalResult.error)
        }
    }

    override fun cancel() {
    }

    private fun workOperation(reporter: ProgressReporter<EmailDetailResult.DownloadFile>) :
            Result<Unit, Exception> = downloadFileMetadata(fileToken)
            .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
            .flatMap(getMetaDataFromJSONResponse)
            .flatMap(downloadFile(reporter))

    private fun newRetryWithNewSessionOperation(reporter: ProgressReporter<EmailDetailResult.DownloadFile>)
            : Result<Unit, Exception> {
        apiClient = GeneralAPIClient(HttpClient.Default(), activeAccount.jwt)
        val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient,
                activeAccount, storage, accountDao)
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when(refreshOperation){
            is Result.Success -> {
                fileServiceAPIClient.authToken = refreshOperation.value
                workOperation(reporter)
            }
            is Result.Failure -> {
                Result.of { throw refreshOperation.error }
            }
        }
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        ex.printStackTrace()
        when (ex) {
            is ServerErrorException -> UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
            else -> UIMessage(resId = R.string.error_downloading_file, args = arrayOf(fileName))
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