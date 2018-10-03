package com.criptext.mail.scenes.linking.data

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.signal.PreKeyBundleShareData
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONObject


class CheckForKeyBundleWorker(
        private val deviceId: Int,
        httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        override val publishFn: (
                LinkingResult.CheckForKeyBundle) -> Unit)
    : BackgroundWorker<LinkingResult.CheckForKeyBundle> {

    override val canBeParallelized = true
    private val apiClient = CheckForKeyBundleAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): LinkingResult.CheckForKeyBundle {
        return LinkingResult.CheckForKeyBundle.Failure(UIMessage(R.string.password_enter_error))
    }

    override fun work(reporter: ProgressReporter<LinkingResult.CheckForKeyBundle>): LinkingResult.CheckForKeyBundle? {
        val operation =
                Result.of {
                    apiClient.getKeyBundle(deviceId)
                }
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
                .flatMap { Result.of { PreKeyBundleShareData.DownloadBundle.fromJSON(JSONObject(it)) } }
        return when (operation){
            is Result.Success -> {
                LinkingResult.CheckForKeyBundle.Success(operation.value)
            }
            is Result.Failure -> {
                catchException(operation.error)
            }
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

}
