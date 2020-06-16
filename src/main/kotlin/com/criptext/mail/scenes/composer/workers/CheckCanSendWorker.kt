package com.criptext.mail.scenes.composer.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.PeerEventsApiHandler
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.EmailDao
import com.criptext.mail.db.dao.PendingEventDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.composer.data.ComposerAPIClient
import com.criptext.mail.scenes.composer.data.ComposerInputData
import com.criptext.mail.scenes.composer.data.ComposerResult
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.peerdata.PeerReadEmailData
import com.github.kittinunf.result.Result
import org.json.JSONObject


class CheckCanSendWorker(httpClient: HttpClient,
                         private val composerInputData: ComposerInputData,
                         private val activeAccount: ActiveAccount,
                         override val publishFn: (ComposerResult.CheckCanSend) -> Unit
                       ) : BackgroundWorker<ComposerResult.CheckCanSend> {

    override val canBeParallelized = false
    private val apiClient = ComposerAPIClient(httpClient, activeAccount.jwt)


    override fun catchException(ex: Exception): ComposerResult.CheckCanSend {
        when(ex){
            is ServerErrorException -> {
                when(ex.errorCode){
                    ServerCodes.MethodNotAllowed -> {
                        val body = ex.body ?: return ComposerResult.CheckCanSend.Success(composerInputData)
                        val json = JSONObject(body)
                        val data = json.getJSONObject("data")
                        val recovery = data.getString("recovery")
                        return ComposerResult.CheckCanSend.Failure(recovery.isNotEmpty(), false)
                    }
                }
            }
        }
        return ComposerResult.CheckCanSend.Success(composerInputData)
    }

    override fun work(reporter: ProgressReporter<ComposerResult.CheckCanSend>)
            : ComposerResult.CheckCanSend? {
        val result = Result.of {
            apiClient.getCanSend()
        }

        return when (result) {
            is Result.Success -> {
                ComposerResult.CheckCanSend.Success(composerInputData)
            }
            is Result.Failure -> {
                catchException(result.error)
            }
        }
    }

    override fun cancel() {
        TODO("not implemented")
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when(ex){
            is ServerErrorException -> {
                UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
            }
            else -> UIMessage(resId = R.string.failed_getting_emails)
        }
    }
}