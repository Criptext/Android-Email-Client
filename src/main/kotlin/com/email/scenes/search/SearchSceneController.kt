package com.email.scenes.search

import com.email.IHostActivity
import com.email.db.KeyValueStorage
import com.email.db.models.ActiveAccount
import com.email.db.models.Contact
import com.email.db.models.Label
import com.email.scenes.ActivityMessage
import com.email.scenes.SceneController
import com.email.scenes.composer.data.ComposerTypes
import com.email.scenes.mailbox.MailboxSceneController
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.mailbox.data.LoadParams
import com.email.scenes.params.ComposerParams
import com.email.scenes.params.EmailDetailParams
import com.email.scenes.search.data.SearchDataSource
import com.email.scenes.search.data.SearchItem
import com.email.scenes.search.data.SearchRequest
import com.email.scenes.search.data.SearchResult
import com.email.scenes.search.ui.SearchResultAdapter
import com.email.scenes.search.ui.SearchThreadAdapter
import com.email.scenes.search.ui.SearchUIObserver

/**
 * Created by danieltigse on 2/2/18.
 */

class SearchSceneController(private val scene: SearchScene,
                            private val model: SearchSceneModel,
                            private val host: IHostActivity,
                            private val activeAccount: ActiveAccount,
                            private val storage: KeyValueStorage,
                            private val dataSource: SearchDataSource): SceneController(){

    private val searchListController = SearchResultListController(
            model, scene.searchListView, scene.threadsListView)

    override val menuResourceId: Int?
        get() = null

    override fun onOptionsItemSelected(itemId: Int) {

    }

    companion object {
        const val MAXIMUM_SEARCH_HISTORY = 10
        const val SEPARATOR = "#Cr1p3tx2018#"
    }

    private val dataSourceListener = { result: SearchResult ->
        when (result) {
            is SearchResult.SearchEmails -> onSearchEmails(result)
        }
    }

    private val onSearchResultListController = object: SearchResultAdapter.OnSearchEventListener {

        override fun onSearchSelected(searchItem: SearchItem) {
            scene.setSearchText(searchItem.subject)
            scene.hideAllListViews()
        }

        override fun onApproachingEnd() {
        }

    }

    private val threadEventListener = object : SearchThreadAdapter.OnThreadEventListener {

        override fun onApproachingEnd() {
            val req = SearchRequest.SearchEmails(
                    queryText = model.queryText,
                    loadParams = LoadParams.NewPage(size = MailboxSceneController.threadsPerPage,
                    oldestEmailThread = model.threads.lastOrNull()),
                    userEmail = activeAccount.userEmail)
            dataSource.submitRequest(req)
        }

        override fun onGoToMail(emailThread: EmailThread) {

            if(emailThread.totalEmails == 1 &&
                    emailThread.latestEmail.labels.contains(Label.defaultItems.draft)){
                return host.goToScene(ComposerParams(
                        fullEmail = emailThread.latestEmail,
                        composerType = ComposerTypes.CONTINUE_DRAFT,
                        userEmail = activeAccount.userEmail,
                        emailDetailActivity = null
                ), true)
            }
            dataSource.submitRequest(SearchRequest.UpdateUnreadStatus(
                    listOf(emailThread), false, Label.defaultItems.inbox))
            host.goToScene(EmailDetailParams(emailThread.threadId, Label.defaultItems.inbox), true)
        }

        override fun onToggleThreadSelection(thread: EmailThread, position: Int) {

        }
    }

    private val observer = object :SearchUIObserver{

        override fun onBackButtonClicked() {
            host.finishScene()
        }

        override fun onInputTextChange(text: String) {
            model.queryText = text
            if(text.isNotBlank()) {
                val req = SearchRequest.SearchEmails(
                        queryText = text,
                        loadParams = LoadParams.Reset(size = MailboxSceneController.threadsPerPage),
                        userEmail = activeAccount.userEmail)
                dataSource.submitRequest(req)
                scene.hideAllListViews()
            }
            else{
                scene.toggleSearchListView(model.searchItems.size)
            }
        }

        override fun onSearchButtonClicked(text: String) {
            saveSearchHistory(text)
        }

    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        scene.attachView(
                searchResultList = VirtualSearchResultList(model),
                searchListener = onSearchResultListController,
                threadsList = VirtualSearchThreadList(model),
                threadListener = threadEventListener,
                observer = observer
        )

        val results = getSearchHistory()
        searchListController.setHistorySearchList(results)

        scene.toggleSearchListView(results.size)

        dataSource.listener = dataSourceListener

        return false
    }

    override fun onStop() {

    }

    override fun onBackPressed(): Boolean {
        return true
    }

    private fun onSearchEmails(result: SearchResult.SearchEmails){
        when(result) {
            is SearchResult.SearchEmails.Success -> {
                if(model.queryText == result.queryText) {
                    val hasReachedEnd = result.emailThreads.size < MailboxSceneController.threadsPerPage
                    if (model.threads.isNotEmpty() && result.isReset)
                        searchListController.populateThreads(result.emailThreads)
                    else
                        searchListController.appendAll(result.emailThreads, hasReachedEnd)
                    scene.toggleThreadListView(result.emailThreads.size)
                }
            }
        }
    }

    private fun saveSearchHistory(value: String) {

        val searchHistory = getSearchHistory().toMutableList()
        if(searchHistory.size == MAXIMUM_SEARCH_HISTORY){
            searchHistory.removeAt(searchHistory.size - 1)
        }
        val newSearchItem = SearchItem(searchHistory.size, value, "")
        newSearchItem.timestampCreated = System.currentTimeMillis()
        searchHistory.add(newSearchItem)

        storage.putStringSet(KeyValueStorage.StringKey.SearchHistory,
                searchHistory.map { "${it.subject}$SEPARATOR${it.timestampCreated}" }.toMutableSet())

    }

    private fun getSearchHistory(): List<SearchItem> {

        val setHistory = storage.getStringSet(KeyValueStorage.StringKey.SearchHistory) ?: return listOf()
        val searchHistory =  setHistory.mapIndexed { index, s ->
            val searchItem = SearchItem(index, s.split(SEPARATOR)[0], "")
            searchItem.timestampCreated = s.split(SEPARATOR)[1].toLong()
            searchItem
        }
        return searchHistory.sortedWith(Comparator { p0, p1 ->
            when {
                p0.timestampCreated!! > p1.timestampCreated!! -> -1
                p0.timestampCreated!! == p1.timestampCreated!! -> 0
                else -> 1
            }
        })
    }

}