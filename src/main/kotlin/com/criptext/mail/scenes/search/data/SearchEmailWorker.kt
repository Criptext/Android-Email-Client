package com.criptext.mail.scenes.search.data

import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.SearchLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.mailbox.data.EmailThread
import com.criptext.mail.scenes.mailbox.data.LoadParams
import com.criptext.mail.utils.UIMessage

class SearchEmailWorker(
        private val userEmail: String,
        private val activeAccount: ActiveAccount,
        private val db: SearchLocalDB,
        private val queryText: String,
        private val loadParams: LoadParams,
        override val publishFn: (
                SearchResult.SearchEmails) -> Unit
): BackgroundWorker<SearchResult.SearchEmails> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): SearchResult.SearchEmails {
        return SearchResult.SearchEmails.Failure(UIMessage(resId = R.string.failed_searching_emails))
    }

    private fun loadThreadsWithParams(): List<EmailThread> = when (loadParams) {
        is LoadParams.NewPage -> db.searchMailsInDB(
                queryText = queryText,
                startDate = loadParams.startDate,
                limit = loadParams.size,
                userEmail = userEmail,
                account = activeAccount)
        is LoadParams.UpdatePage -> db.searchMailsInDB(
                queryText = queryText,
                startDate = null,
                limit = loadParams.size,
                userEmail = userEmail,
                account = activeAccount)
        is LoadParams.Reset -> db.searchMailsInDB(
                queryText = queryText,
                startDate = null,
                limit = loadParams.size,
                userEmail = userEmail,
                account = activeAccount)
    }

    override fun work(reporter: ProgressReporter<SearchResult.SearchEmails>): SearchResult.SearchEmails? {
        val emailThreads = loadThreadsWithParams()

        return SearchResult.SearchEmails.Success(
                emailThreads = emailThreads,
                isReset = loadParams is LoadParams.Reset,
                queryText = queryText)
    }

    override fun cancel() {
    }

}