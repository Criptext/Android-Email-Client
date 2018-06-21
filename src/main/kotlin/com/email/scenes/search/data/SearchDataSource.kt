package com.email.scenes.search.data

import com.email.bgworker.BackgroundWorkManager
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.WorkRunner
import com.email.db.SearchLocalDB

/**
 * Created by danieltigse on 2/5/18.
 */

class SearchDataSource(
        private val searchLocalDB: SearchLocalDB,
        override val runner: WorkRunner)
    : BackgroundWorkManager<SearchRequest, SearchResult>(){

    override fun createWorkerFromParams(params: SearchRequest, flushResults: (SearchResult) -> Unit): BackgroundWorker<*> {

        return when (params) {

            is SearchRequest.SearchEmails -> SearchEmailWorker(
                    db = searchLocalDB,
                    queryText = params.queryText,
                    loadParams = params.loadParams,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is SearchRequest.UpdateUnreadStatus -> UpdateUnreadStatusWorker(
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
