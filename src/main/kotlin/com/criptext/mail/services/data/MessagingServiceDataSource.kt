package com.criptext.mail.services.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.dao.AccountDao

class MessagingServiceDataSource(override val runner: WorkRunner,
                                 private val httpClient: HttpClient,
                                 private val accountDao: AccountDao)
    : BackgroundWorkManager<MessagingServiceRequest, MessagingServiceResult>() {
    override fun createWorkerFromParams(params: MessagingServiceRequest,
                                        flushResults: (MessagingServiceResult) -> Unit):
            BackgroundWorker<*> {
        return when (params) {
            is MessagingServiceRequest.RefreshPushTokenOnServer -> RefreshPushTokenOnServerWorker(
                    httpClient = httpClient,
                    pushToken = params.token,
                    accountDao = accountDao,
                    publishFn = { result ->
                        flushResults(result)
                    })
        }
    }
}