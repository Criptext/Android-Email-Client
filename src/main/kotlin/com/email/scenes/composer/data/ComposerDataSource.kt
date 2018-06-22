package com.email.scenes.composer.data

import com.email.bgworker.BackgroundWorker
import com.email.bgworker.BackgroundWorkManager
import com.email.bgworker.WorkRunner
import com.email.db.ComposerLocalDB
import com.email.db.dao.EmailInsertionDao
import com.email.db.models.ActiveAccount

/**
 * Created by gabriel on 2/26/18.
 */

class ComposerDataSource(
        private val composerLocalDB: ComposerLocalDB,
        private val activeAccount: ActiveAccount,
        private val emailInsertionDao: EmailInsertionDao,
        override val runner: WorkRunner)
    : BackgroundWorkManager<ComposerRequest, ComposerResult>() {

    override fun createWorkerFromParams(params: ComposerRequest,
                                        flushResults: (ComposerResult) -> Unit)
            : BackgroundWorker<*> {
        return when(params) {
            is ComposerRequest.GetAllContacts -> LoadContactsWorker(composerLocalDB, { res ->
                flushResults(res)
            })
            is ComposerRequest.SaveEmailAsDraft -> SaveEmailWorker(
                    threadId = params.threadId,
                    emailId = params.emailId, composerInputData = params.composerInputData,
                    account = activeAccount, dao = emailInsertionDao,
                    onlySave = params.onlySave, publishFn = { res -> flushResults(res) })
            is ComposerRequest.DeleteDraft -> DeleteDraftWorker(
                    emailId = params.emailId,
                    db = composerLocalDB,
                    publishFn = { res -> flushResults(res) })
        }
    }

}