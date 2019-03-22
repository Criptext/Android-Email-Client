package com.criptext.mail.scenes.search.data

import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.SearchLocalDB
import com.criptext.mail.db.models.ActiveAccount

/**
 * Created by danieltigse on 2/5/18.
 */

class SearchDataSource(
        private val searchLocalDB: SearchLocalDB,
        private val activeAccount: ActiveAccount,
        override val runner: WorkRunner)
    : BackgroundWorkManager<SearchRequest, SearchResult>(){

    override fun createWorkerFromParams(params: SearchRequest, flushResults: (SearchResult) -> Unit): BackgroundWorker<*> {

        return when (params) {

            is SearchRequest.SearchEmails -> SearchEmailWorker(
                    activeAccount = activeAccount,
                    db = searchLocalDB,
                    queryText = params.queryText,
                    loadParams = params.loadParams,
                    userEmail = params.userEmail,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is SearchRequest.UpdateUnreadStatus -> UpdateUnreadStatusWorker(
                    activeAccount = activeAccount,
                    db = searchLocalDB,
                    currentLabel = params.currentLabel,
                    emailThreads = params.emailThreads,
                    updateUnreadStatus = params.updateUnreadStatus,
                    publishFn = { result ->
                        flushResults(result)
                    })
        }
    }

}
