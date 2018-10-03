package com.criptext.mail.scenes.signin.data

import com.criptext.mail.R
import com.criptext.mail.aes.AESUtil
import com.criptext.mail.api.Hosts
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.signup.data.SignUpAPIClient
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalEncryptedData
import com.criptext.mail.utils.Encoding
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.google.android.gms.common.util.IOUtils
import java.nio.file.StandardCopyOption
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.FileOutputStream
import java.io.OutputStream






class LinkDataWorker(private val authorizerId: Int,
                     val activeAccount: ActiveAccount,
                     private val key: String,
                     private val dataAddress: String,
                     private val signalClient: SignalClient,
                     private val db: AppDatabase,
                     override val publishFn: (SignInResult) -> Unit)
    : BackgroundWorker<SignInResult.LinkData> {

    private val fileHttpClient = HttpClient.Default(Hosts.fileTransferServer, HttpClient.AuthScheme.jwt,
            14000L, 7000L)
    private val apiClient = SignInAPIClient(fileHttpClient)
    private val dataWriter = UserDataWriter(db)

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

    override fun work(reporter: ProgressReporter<SignInResult.LinkData>): SignInResult.LinkData? {
        val params = mutableMapOf<String, String>()
        params["id"] = dataAddress
        val result =  Result.of { apiClient.getFileStream(activeAccount.jwt, params) }
                .flatMap { readIntoFile(it) }
                .flatMap { Result.of { Pair(signalClient.decryptBytes(activeAccount.recipientId,
                        authorizerId,
                        SignalEncryptedData(key, SignalEncryptedData.Type.preKey)),
                        it
                )
                }}
                .flatMap { Result.of { AESUtil.decryptFileByChunks(it.second, it.first) } }
                .flatMap { Result.of {
                    val decryptedFile = File(it)
                    dataWriter.createDBFromFile(decryptedFile)
                }}


        return when (result) {
            is Result.Success ->{
                SignInResult.LinkData.Success()
            }
            is Result.Failure -> catchException(result.error)
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        UIMessage(resId = R.string.forgot_password_error)
    }

}