package com.email.scenes.composer.data

import android.accounts.NetworkErrorException
import com.email.R
import com.email.api.HttpClient
import com.email.api.HttpErrorHandlingHelper
import com.email.api.ServerErrorException
import com.email.bgworker.BackgroundWorker
import com.email.utils.UIMessage
import com.email.utils.file.ChunkFileReader
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class UploadAttachmentWorker(private val filepath: String,
                             httpClient: HttpClient,
                             fileServiceAuthToken: String,
                             override val publishFn: (ComposerResult.UploadFile) -> Unit)
    : BackgroundWorker<ComposerResult.UploadFile> {
    override val canBeParallelized = false

    val fileServiceAPIClient = FileServiceAPIClient(httpClient, fileServiceAuthToken)

    override fun catchException(ex: Exception): ComposerResult.UploadFile {
         return ComposerResult.UploadFile.Failure(filepath, createErrorMessage(ex))
    }



    private fun uploadFile(file: File): (String) -> Result<Unit, Exception> = { fileToken ->
            Result.of {
                val onNewChunkRead: (ByteArray, Int) -> Unit = { chunk, index ->
                    fileServiceAPIClient.uploadChunk(chunk = chunk, fileName = file.name,
                            part = index, fileToken = fileToken)
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
                    chunkSize = chunkSize, fileSize = fileSize.toInt(), totalChunks = totalChunks)
        }

    override fun work(): ComposerResult.UploadFile? {
        val file = File(filepath)

        val result = registerFile(file)
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
                .flatMap(getFileTokenFromJSONResponse)
                .flatMap(uploadFile(file))

        return when (result) {
            is Result.Success -> ComposerResult.UploadFile.Success(filepath)
            is Result.Failure -> ComposerResult.UploadFile.Failure(filepath,
                    createErrorMessage(result.error))
        }
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) { // these are not the real errors TODO fix!
            is JSONException -> UIMessage(resId = R.string.json_error_exception)
            is ServerErrorException -> {
                if(ex.errorCode == 400) {
                    UIMessage(resId = R.string.duplicate_name_error_exception)
                } else {
                    UIMessage(resId = R.string.server_error_exception)
                }
            }
            is NetworkErrorException -> UIMessage(resId = R.string.network_error_exception)
            else -> UIMessage(resId = R.string.fail_register_try_again_error_exception)
        }
    }
    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        private const val chunkSize = 512 * 1024 * 1024

    }

}