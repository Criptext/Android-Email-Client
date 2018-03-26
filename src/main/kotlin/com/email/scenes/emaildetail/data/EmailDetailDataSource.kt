package com.email.scenes.emaildetail.data

import com.email.bgworker.BackgroundWorker
import com.email.bgworker.WorkHandler
import com.email.bgworker.WorkRunner
import com.email.db.EmailDetailLocalDB
import com.email.db.models.ActiveAccount
import com.email.scenes.emaildetail.workers.DecryptMailWorker
import com.email.scenes.emaildetail.workers.LoadFullEmailsFromThreadWorker
import com.email.scenes.emaildetail.workers.UnsendFullEmailWorker
import com.email.signal.SignalClient

/**
 * Created by sebas on 3/12/18.
 */

class EmailDetailDataSource(private val signalClient: SignalClient,
                            private val activeAccount: ActiveAccount,
                            override val runner: WorkRunner,
                            private val emailDetailLocalDB: EmailDetailLocalDB)
    : WorkHandler<EmailDetailRequest, EmailDetailResult>()
{

    override fun createWorkerFromParams(params: EmailDetailRequest,
                                        flushResults: (EmailDetailResult) -> Unit):
            BackgroundWorker<*> {
        return when (params) {
            is EmailDetailRequest.LoadFullEmailsFromThreadId -> LoadFullEmailsFromThreadWorker(
                    db = emailDetailLocalDB,
                    threadId = params.threadId,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is EmailDetailRequest.UnsendFullEmailFromEmailId -> UnsendFullEmailWorker(
                    db = emailDetailLocalDB,
                    emailId = params.emailId,
                    position = params.position,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is EmailDetailRequest.DecryptMail -> DecryptMailWorker(
                    signalClient = signalClient,
                    deviceId = params.deviceId,
                    emailId = params.emailId,
                    encryptedMessage = params.encryptedText,
                    activeAccount = activeAccount,
                    recipientId = params.recipientId,
                    publishFn = { result ->
                        flushResults(result)
                    })
        }
    }
}
