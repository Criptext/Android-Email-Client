package com.criptext.mail.utils.generaldatasource.workers

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
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.signup.data.SignUpAPIClient
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalEncryptedData
import com.criptext.mail.utils.Encoding
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.criptext.mail.utils.sha256
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import com.google.android.gms.common.util.IOUtils
import java.nio.file.StandardCopyOption
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.IOException
import java.util.zip.GZIPInputStream








class LinkDataWorker(private val authorizerId: Int,
                     val filesDir: File,
                     private var activeAccount: ActiveAccount,
                     private val key: String,
                     private val dataAddress: String,
                     private val signalClient: SignalClient,
                     private val storage: KeyValueStorage,
                     private val db: AppDatabase,
                     override val publishFn: (GeneralResult) -> Unit)
    : BackgroundWorker<GeneralResult.LinkData> {

    private val fileHttpClient = HttpClient.Default(Hosts.fileTransferServer, HttpClient.AuthScheme.jwt,
            14000L, 7000L)
    private val apiClient = GeneralAPIClient(fileHttpClient, activeAccount.jwt)
    private val dataWriter = UserDataWriter(db, filesDir)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): GeneralResult.LinkData {
        return GeneralResult.LinkData.Failure(createErrorMessage(ex), ex)
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

    override fun work(reporter: ProgressReporter<GeneralResult.LinkData>): GeneralResult.LinkData? {
        val result =  workOperation(reporter)

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(result)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation(reporter)
        else
            result


        return when (finalResult) {
            is Result.Success ->{
                GeneralResult.LinkData.Success()
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

    private fun workOperation(reporter: ProgressReporter<GeneralResult.LinkData>) : Result<Unit, Exception> = Result.of {
        val params = mutableMapOf<String, String>()
        params["id"] = dataAddress
        reporter.report(GeneralResult.LinkData.Progress(UIMessage(R.string.downloading_mailbox), 70))
        apiClient.getFileStream(params)
    }
            .flatMap { readIntoFile(it) }
            .flatMap { Result.of {
                reporter.report(GeneralResult.LinkData.Progress(UIMessage(R.string.processing_mailbox), 80))
                Pair(signalClient.decryptBytes(activeAccount.recipientId,
                        authorizerId,
                        SignalEncryptedData(key, SignalEncryptedData.Type.preKey)),
                        it
                )
            }}
            .flatMap { Result.of {
                reporter.report(GeneralResult.LinkData.Progress(UIMessage(R.string.processing_mailbox), 85))
                AESUtil.decryptFileByChunks(it.second, it.first)
            } }
            .flatMap { Result.of {
                reporter.report(GeneralResult.LinkData.Progress(UIMessage(R.string.processing_mailbox), 90))
                decompress(it)
            } }
            .flatMap { Result.of {
                reporter.report(GeneralResult.LinkData.Progress(UIMessage(R.string.processing_mailbox), 95))
                val decryptedFile = File(it)
                deleteLocalData()
                dataWriter.createDBFromFile(decryptedFile)
            }}

    private fun newRetryWithNewSessionOperation(reporter: ProgressReporter<GeneralResult.LinkData>)
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

    private fun deleteLocalData(){
        db.fileKeyDao().nukeTable()
        db.emailContactDao().nukeTable()
        db.emailExternalSessionDao().nukeTable()
        db.emailLabelDao().nukeTable()
        db.fileDao().nukeTable()
        db.contactDao().nukeTable()
        db.pendingEventDao().nukeTable()
        db.feedDao().nukeTable()
        db.labelDao().nukeTable()
        db.emailDao().nukeTable()

        val defaultLabels = Label.defaultItems.toList()
        db.labelDao().insertAll(defaultLabels)
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        UIMessage(resId = R.string.forgot_password_error)
    }

}