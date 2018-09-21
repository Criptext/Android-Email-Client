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
import com.criptext.mail.utils.sha256
import com.github.kittinunf.result.Result

class LinkAuthDenyWorker(private val untrustedDeviceInfo: UntrustedDeviceInfo,
                         private val activeAccount: ActiveAccount,
                         private val httpClient: HttpClient,
                         override val publishFn: (GeneralResult.LinkDeny) -> Unit
                          ) : BackgroundWorker<GeneralResult.LinkDeny> {

    override val canBeParallelized = false

    private val apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): GeneralResult.LinkDeny {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(reporter: ProgressReporter<GeneralResult.LinkDeny>)
            : GeneralResult.LinkDeny? {
        if(untrustedDeviceInfo.recipientId != activeAccount.recipientId)
            return GeneralResult.LinkDeny.Failure(UIMessage(R.string.server_error_exception))

        val operation = Result.of {
            apiClient.postLinkDeny(untrustedDeviceInfo.deviceId)
        }

        return when (operation){
            is Result.Success -> {
                GeneralResult.LinkDeny.Success()
            }
            is Result.Failure -> {
                GeneralResult.LinkDeny.Failure(UIMessage(R.string.server_error_exception))
            }
        }
    }

    override fun cancel() {
        TODO("not implemented")
    }
}