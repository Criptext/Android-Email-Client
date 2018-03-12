package com.email.scenes.emaildetail.data

import com.email.bgworker.BackgroundWorker
import com.email.bgworker.WorkHandler
import com.email.bgworker.WorkRunner
import com.email.db.EmailDetailLocalDB

/**
 * Created by sebas on 3/12/18.
 */

/*class EmailDetailDataSource(override val runner: WorkRunner,
                            private val emailDetailAPIClient: EmailDetailAPIClient,
                            private val emailDetailLocalDB: EmailDetailLocalDB)
    : WorkHandler<EmailDetailRequest, EmailDetailResult>() {
    override fun createWorkerFromParams(params: EmailDetailRequest,
                                        flushResults: (EmailDetailResult) -> Unit): BackgroundWorker<*> {
*//*        return when (params) {
        }*//*
    }
}*/

class EmailDetailDataSource(private val runner: WorkRunner,
                            private val emailDetailAPIClient: EmailDetailAPIClient,
                            private val emailDetailLocalDB: EmailDetailLocalDB) {

}
