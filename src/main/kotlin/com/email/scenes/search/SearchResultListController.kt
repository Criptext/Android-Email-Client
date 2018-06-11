package com.email.scenes.search

import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.search.data.SearchItem
import com.email.utils.virtuallist.VirtualListView

/**
 * Created by danieltigse on 02/14/18.
 */

class SearchResultListController(
        private val model: SearchSceneModel,
        private var searchListView: VirtualListView?,
        private var threadsListView: VirtualListView?) {

    fun setHistorySearchList(searchItems: List<SearchItem>) {
        model.searchItems.clear()
        model.searchItems.addAll(searchItems)
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
