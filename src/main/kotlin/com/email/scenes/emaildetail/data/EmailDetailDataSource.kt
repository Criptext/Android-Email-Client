package com.email.scenes.emaildetail.data

import com.email.bgworker.BackgroundWorker
import com.email.bgworker.WorkHandler
import com.email.bgworker.WorkRunner
import com.email.db.EmailDetailLocalDB
import com.email.db.models.ActiveAccount
import com.email.scenes.emaildetail.workers.*
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

            is EmailDetailRequest.GetSelectedLabels -> GetSelectedLabelsWorker(
                    db = emailDetailLocalDB,
                    threadId = params.threadId,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is EmailDetailRequest.UpdateEmailThreadsLabelsRelations -> UpdateEmailLabelsRelationsWorker(
                    db = emailDetailLocalDB,
                    threadId = params.threadId,
                    chosenLabel = params.chosenLabel,
                    selectedLabels = params.selectedLabels,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is EmailDetailRequest.UpdateUnreadStatus -> UpdateUnreadStatusWorker(
                    db = emailDetailLocalDB,
                    threadId = params.threadId,
                    updateUnreadStatus = params.updateUnreadStatus,
                    publishFn = { result ->
                        flushResults(result)
                    })
        }
    }
}
