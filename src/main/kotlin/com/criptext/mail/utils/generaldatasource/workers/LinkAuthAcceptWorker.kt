package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.UntrustedDeviceInfo
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result
import org.json.JSONObject

class LinkAuthAcceptWorker(private val untrustedDeviceInfo: UntrustedDeviceInfo,
                           private val activeAccount: ActiveAccount,
                           private val httpClient: HttpClient,
                           override val publishFn: (GeneralResult.LinkAccept) -> Unit
                          ) : BackgroundWorker<GeneralResult.LinkAccept> {

    override val canBeParallelized = false

    private val apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): GeneralResult.LinkAccept {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(reporter: ProgressReporter<GeneralResult.LinkAccept>)
            : GeneralResult.LinkAccept? {
        if(untrustedDeviceInfo.recipientId != activeAccount.recipientId)
            return GeneralResult.LinkAccept.Failure(UIMessage(R.string.server_error_exception))

        val operation = Result.of {
            JSONObject(apiClient.postLinkAccept(untrustedDeviceInfo.deviceId)).getInt("deviceId")
        }

        return when (operation){
            is Result.Success -> {
                GeneralResult.LinkAccept.Success(operation.value, untrustedDeviceInfo.deviceId)
            }
            is Result.Failure -> {
                GeneralResult.LinkAccept.Failure(UIMessage(R.string.server_error_exception))
            }
        }
    }

    override fun cancel() {
        TODO("not implemented")
    }
}