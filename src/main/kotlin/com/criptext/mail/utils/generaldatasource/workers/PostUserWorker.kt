package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.api.Hosts
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.signal.PreKeyBundleShareData
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONObject

class PostUserWorker(private val keyBundle: PreKeyBundleShareData.DownloadBundle?,
                     private val httpClient: HttpClient,
                     private val randomId: String,
                     private val filePath: String,
                     private val deviceId: Int,
                     private val fileKey: ByteArray,
                     private val activeAccount: ActiveAccount,
                     private val storage: KeyValueStorage,
                     private val accountDao: AccountDao,
                     private val signalClient: SignalClient,
                     override val publishFn: (GeneralResult.PostUserData) -> Unit
                          ) : BackgroundWorker<GeneralResult.PostUserData> {

    override val canBeParallelized = false

    private val fileHttpClient = HttpClient.Default(Hosts.fileTransferServer, HttpClient.AuthScheme.jwt,
            14000L, 7000L)

    private val fileApiClient = GeneralAPIClient(fileHttpClient, activeAccount.jwt)
    private val apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): GeneralResult.PostUserData {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun getKeyBundle(): Result<Unit, Exception> {
        if(keyBundle != null) {
            return Result.of {
                signalClient.createSessionsFromBundles(listOf(keyBundle))
            }
        }
        return Result.of { apiClient.getKeyBundle(deviceId) }
                .flatMap {
                    Result.of {
                        val bundleJSON = JSONObject(it)
                        val downloadedBundle =
                                PreKeyBundleShareData.DownloadBundle.fromJSON(bundleJSON)
                        signalClient.createSessionsFromBundles(listOf(downloadedBundle))
                    }
                }
    }

    override fun work(reporter: ProgressReporter<GeneralResult.PostUserData>)
            : GeneralResult.PostUserData? {

        val operation = workOperation()

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(operation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            operation

        return when (finalResult){
            is Result.Success -> {
                GeneralResult.PostUserData.Success()
            }
            is Result.Failure -> {
                GeneralResult.PostUserData.Failure(UIMessage(R.string.server_error_exception))
            }
        }
    }

    override fun cancel() {
        TODO("not implemented")
    }

    private fun workOperation() : Result<String, Exception> = getKeyBundle()
            .flatMap { Result.of {
                fileApiClient.postFileStream(filePath, randomId)
            } }
            .flatMap { Result.of {
                signalClient.encryptBytes(activeAccount.recipientId, deviceId, fileKey)
            } }
            .flatMap { Result.of {
                apiClient.postLinkDataReady(deviceId, it.encryptedB64)
            } }

    private fun newRetryWithNewSessionOperation()
            : Result<String, Exception> {
        val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient,
                activeAccount, storage, accountDao)
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when(refreshOperation){
            is Result.Success -> {
                val account = ActiveAccount.loadFromStorage(storage)!!
                apiClient.token = account.jwt
                fileApiClient.token = account.jwt
                workOperation()
            }
            is Result.Failure -> {
                Result.of { throw refreshOperation.error }
            }
        }
    }
}