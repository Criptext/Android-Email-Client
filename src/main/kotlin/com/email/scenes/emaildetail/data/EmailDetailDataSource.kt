package com.email.scenes.emaildetail.data

import com.email.bgworker.BackgroundWorker
import com.email.bgworker.WorkHandler
import com.email.bgworker.WorkRunner
import com.email.db.EmailDetailLocalDB
import com.email.scenes.emaildetail.workers.LoadFullEmailsFromThreadWorker

/**
 * Created by sebas on 3/12/18.
 */

class EmailDetailDataSource(override val runner: WorkRunner,
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
        }
    }
}
