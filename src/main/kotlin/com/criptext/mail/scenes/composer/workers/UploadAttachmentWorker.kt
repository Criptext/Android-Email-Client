package com.criptext.mail.scenes.composer.workers

import android.accounts.NetworkErrorException
import com.criptext.mail.R
import com.criptext.mail.aes.AESUtil
import com.criptext.mail.api.CriptextAPIClient
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.composer.data.ComposerResult
import com.criptext.mail.scenes.composer.data.FileServiceAPIClient
import com.criptext.mail.utils.EmailUtils
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.file.ChunkFileReader
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class UploadAttachmentWorker(private val filesSize: Long,
                             private val filepath: String,
                             private val httpClient: HttpClient,
                             private val activeAccount: ActiveAccount,
                             private val storage: KeyValueStorage,
                             private val accountDao: AccountDao,
                             val fileKey: String?,
                             override val publishFn: (ComposerResult.UploadFile) -> Unit)
    : BackgroundWorker<ComposerResult.UploadFile> {
    override val canBeParallelized = false
    private lateinit var apiClient: CriptextAPIClient
    private val fileServiceAPIClient = FileServiceAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): ComposerResult.UploadFile =
            if(ex is ServerErrorException) {
                when {
                    ex.errorCode == ServerCodes.Unauthorized ->
                        ComposerResult.UploadFile.Unauthorized(UIMessage(R.string.device_removed_remotely_exception))
                    ex.errorCode == ServerCodes.Forbidden ->
                        ComposerResult.UploadFile.Forbidden()
                    ex.errorCode == ServerCodes.PayloadTooLarge ->
                        ComposerResult.UploadFile.PayloadTooLarge(filepath, ex.headers!!)
                    else -> ComposerResult.UploadFile.Failure(filepath, createErrorMessage(ex))
                }
            }
            else ComposerResult.UploadFile.Failure(filepath, createErrorMessage(ex))


    private fun MaxFilesExceeds(): ComposerResult.UploadFile =
            ComposerResult.UploadFile.MaxFilesExceeds(filepath)


    private fun uploadFile(file: File, reporter: ProgressReporter<ComposerResult.UploadFile>): (String) -> Result<Unit, Exception> = { fileToken ->
        reporter.report(ComposerResult.UploadFile.Register(file.absolutePath, fileToken))
        Result.of {

            val chunks = (file.length() / chunkSize).toInt() + 1
            val onNewChunkRead: (ByteArray, Int) -> Unit = { chunk, index ->
                reporter.report(ComposerResult.UploadFile.Progress(file.absolutePath,
                        index * 100 / chunks))
                fileServiceAPIClient.uploadChunk(chunk = if(fileKey != null)
                                                            AESUtil(fileKey).encrypt(chunk)
                                                        else
                                                            chunk,
                        fileName = file.name,
                        part = index + 1, fileToken = fileToken).body
            }
            ChunkFileReader.read(file, chunkSize, onNewChunkRead)
        }
    }

    private val getFileTokenFromJSONResponse: (String) -> Result<String, Exception> = { stringResponse ->
        Result.of {
            val jsonObject = JSONObject(stringResponse)
            jsonObject.getString("filetoken")
        }
    }

    private fun registerFile(file: File): Result<String, Exception> =
        Result.of {
            val fileSize = file.length()
            val totalChunks = (fileSize / chunkSize).toInt() + 1

            fileServiceAPIClient.registerFile(fileName = file.name,
                    chunkSize = chunkSize, fileSize = fileSize.toInt(), totalChunks = totalChunks).body
        }

    override fun work(reporter: ProgressReporter<ComposerResult.UploadFile>)
            : ComposerResult.UploadFile? {
        val file = File(filepath)
        val size = filesSize + file.length()
        if(size > EmailUtils.ATTACHMENT_SIZE_LIMIT){
            return MaxFilesExceeds()
        }
        val result = workOperation(file, reporter)

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(result)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation(file, reporter)
        else
            result


        return when (finalResult) {
            is Result.Success -> ComposerResult.UploadFile.Success(filepath, size)
            is Result.Failure -> catchException(finalResult.error)
        }
    }

    private fun workOperation(file: File, reporter: ProgressReporter<ComposerResult.UploadFile>) :
            Result<Unit, Exception> = registerFile(file)
            .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
            .flatMap(getFileTokenFromJSONResponse)
            .flatMap(uploadFile(file, reporter))

    private fun newRetryWithNewSessionOperation(file: File, reporter: ProgressReporter<ComposerResult.UploadFile>)
            : Result<Unit, Exception> {
        apiClient = GeneralAPIClient(HttpClient.Default(), activeAccount.jwt)
        val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient,
                activeAccount, storage, accountDao)
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when(refreshOperation){
            is Result.Success -> {
                val account = ActiveAccount.loadFromStorage(storage)!!
                fileServiceAPIClient.authToken = account.jwt
                workOperation(file, reporter)
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
            is NetworkErrorException -> UIMessage(resId = R.string.general_network_error_exception)
            else -> UIMessage(resId = R.string.unable_to_upload, args = arrayOf(filepath))
        }
    }
    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    companion object {
        private const val chunkSize = 512 * 1024

    }

}