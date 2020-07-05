package com.criptext.mail.scenes.signin.workers

import com.criptext.mail.R
import com.criptext.mail.aes.AESUtil
import com.criptext.mail.api.Hosts
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.signin.data.SignInAPIClient
import com.criptext.mail.scenes.signin.data.SignInResult
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalEncryptedData
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.exceptions.SyncFileException
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import com.google.android.gms.common.util.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream








class LinkDataWorker(private val authorizerId: Int,
                     private val filesDir: File,
                     private var activeAccount: ActiveAccount,
                     private val key: String,
                     private val dataAddress: String,
                     private val signalClient: SignalClient,
                     private val storage: KeyValueStorage,
                     private val db: AppDatabase,
                     override val publishFn: (SignInResult) -> Unit)
    : BackgroundWorker<SignInResult.LinkData> {

    private val fileHttpClient = HttpClient.Default(Hosts.fileTransferServer, HttpClient.AuthScheme.jwt,
            14000L, 7000L)
    private val apiClient = SignInAPIClient(fileHttpClient)
    private val dataWriter = UserDataWriter(db, filesDir)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): SignInResult.LinkData {
        return SignInResult.LinkData.Failure(createErrorMessage(ex), ex)
    }

    private fun readIntoFile(inStream: InputStream): Result<File, Exception>{
        return Result.of {
            val targetFile = createTempFile("downloaded_data_file")
            val outStream = FileOutputStream(targetFile)

            val buffer = ByteArray(8 * 1024)
            var bytesRead  = inStream.read(buffer)
            while (bytesRead != -1) {
                outStream.write(buffer, 0, bytesRead)
                bytesRead = inStream.read(buffer)
            }
            IOUtils.closeQuietly(inStream)
            IOUtils.closeQuietly(outStream)
            targetFile
        }
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

    override fun work(reporter: ProgressReporter<SignInResult.LinkData>): SignInResult.LinkData? {
        val result =  workOperation(reporter)

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(result)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation(reporter)
        else
            result


        return when (finalResult) {
            is Result.Success ->{
                SignInResult.LinkData.Success()
            }
            is Result.Failure -> {
                finalResult.error.printStackTrace()
                catchException(finalResult.error)
            }
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    private fun workOperation(reporter: ProgressReporter<SignInResult.LinkData>) : Result<Unit, Exception> = Result.of {
        val params = mutableMapOf<String, String>()
        params["id"] = dataAddress
        reporter.report(SignInResult.LinkData.Progress(UIMessage(R.string.downloading_mailbox), 70))
        apiClient.getFileStream(activeAccount.jwt, params)
    }
            .flatMap { readIntoFile(it) }
            .flatMap { Result.of {
                reporter.report(SignInResult.LinkData.Progress(UIMessage(R.string.processing_mailbox), 80))
                Pair(signalClient.decryptBytes(activeAccount.recipientId,
                        authorizerId,
                        SignalEncryptedData(key, SignalEncryptedData.Type.preKey)),
                        it
                )
            }}
            .flatMap { Result.of {
                reporter.report(SignInResult.LinkData.Progress(UIMessage(R.string.processing_mailbox), 85))
                AESUtil.decryptFileByChunks(it.second, it.first)
            } }
            .flatMap { Result.of {
                reporter.report(SignInResult.LinkData.Progress(UIMessage(R.string.processing_mailbox), 90))
                decompress(it)
            } }
            .flatMap { Result.of {
                reporter.report(SignInResult.LinkData.Progress(UIMessage(R.string.processing_mailbox), 95))
                val decryptedFile = File(it)
                dataWriter.createDBFromFile(decryptedFile, storage)
            }}

    private fun newRetryWithNewSessionOperation(reporter: ProgressReporter<SignInResult.LinkData>)
            : Result<Unit, Exception> {
        val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient,
                activeAccount, storage, db.accountDao())
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when(refreshOperation){
            is Result.Success -> {
                val account = ActiveAccount.loadFromStorage(storage)!!
                activeAccount = account
                workOperation(reporter)
            }
            is Result.Failure -> {
                Result.of { throw refreshOperation.error }
            }
        }
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