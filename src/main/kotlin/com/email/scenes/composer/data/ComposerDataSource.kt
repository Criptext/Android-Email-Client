package com.email.scenes.composer.data

import com.email.bgworker.BackgroundWorker
import com.email.bgworker.WorkHandler
import com.email.bgworker.WorkRunner
import com.email.db.ComposerLocalDB
import com.email.db.models.ActiveAccount

/**
 * Created by gabriel on 2/26/18.
 */

class ComposerDataSource(
        private val composerLocalDB: ComposerLocalDB,
        override val runner: WorkRunner)
    : WorkHandler<ComposerRequest, ComposerResult>() {

    override fun createWorkerFromParams(params: ComposerRequest, flushResults: (ComposerResult) -> Unit): BackgroundWorker<*> {
        return when(params) {
            is ComposerRequest.GetAllContacts -> LoadContactsWorker(composerLocalDB, { res ->
                flushResults(res)
            })
            is ComposerRequest.SaveEmailAsDraft -> SaveEmailWorker(params.threadId, params.emailId,
                    params.composerInputData, composerLocalDB, params.onlySave, { res ->
                flushResults(res)
            })
        }
    }

}