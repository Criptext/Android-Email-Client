package com.criptext.mail.scenes.search

import com.criptext.mail.scenes.mailbox.data.EmailThread
import com.criptext.mail.scenes.search.data.SearchItem
import com.criptext.mail.utils.virtuallist.VirtualListView

/**
 * Created by danieltigse on 02/14/18.
 */

class SearchResultListController(
        private val model: SearchSceneModel,
        private var searchListView: VirtualListView?,
        private var threadsListView: VirtualListView?) {

    fun updateHistorySearchList() {
        searchListView?.notifyDataSetChanged()
    }

    fun populateThreads(mailboxThreads: List<EmailThread>) {
        model.threads.clear()
        model.threads.addAll(mailboxThreads)
        threadsListView?.notifyDataSetChanged()
    }

    fun appendAll(loadedThreads : List<EmailThread>, hasReachedEnd: Boolean) {
        model.threads.addAll(loadedThreads)

        model.hasReachedEnd = hasReachedEnd
        threadsListView?.notifyDataSetChanged()
    }

}
