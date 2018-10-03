package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.api.Hosts
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.signal.PreKeyBundleShareData
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import org.json.JSONArray

class PostUserWorker(private val httpClient: HttpClient,
                     private val filePath: String,
                     private val deviceId: Int,
                     private val fileKey: ByteArray,
                     private val activeAccount: ActiveAccount,
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
        return Result.of { apiClient.findKeyBundles(listOf(activeAccount.recipientId), emptyMap()) }
                .flatMap {
                    Result.of {
                        val bundlesJSONArray = JSONArray(it)
                        if (bundlesJSONArray.length() > 0) {
                            val downloadedBundles =
                                    PreKeyBundleShareData.DownloadBundle.fromJSONArray(bundlesJSONArray)
                            signalClient.createSessionsFromBundles(
                                    downloadedBundles.filter { it.shareData.deviceId == deviceId }
                            )
                        }
                    }
                }
    }

    override fun work(reporter: ProgressReporter<GeneralResult.PostUserData>)
            : GeneralResult.PostUserData? {

        val operation = getKeyBundle()
        .flatMap { Result.of {
            fileApiClient.postFileStream(filePath)
        } }
        .flatMap { Result.of {
            Pair(signalClient.encryptBytes(activeAccount.recipientId, deviceId, fileKey), it)
        } }
        .flatMap { Result.of {
            apiClient.postLinkDataAddress(deviceId, it.second, it.first.encryptedB64)
            it.first.encryptedB64
        } }

        return when (operation){
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
}