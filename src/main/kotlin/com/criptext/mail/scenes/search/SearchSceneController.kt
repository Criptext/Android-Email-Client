package com.criptext.mail.scenes.search

import com.criptext.mail.IHostActivity
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.composer.data.ComposerType
import com.criptext.mail.scenes.mailbox.MailboxSceneController
import com.criptext.mail.scenes.mailbox.data.EmailThread
import com.criptext.mail.scenes.mailbox.data.LoadParams
import com.criptext.mail.scenes.params.ComposerParams
import com.criptext.mail.scenes.params.EmailDetailParams
import com.criptext.mail.scenes.search.data.SearchDataSource
import com.criptext.mail.scenes.search.data.SearchRequest
import com.criptext.mail.scenes.search.data.SearchResult
import com.criptext.mail.scenes.search.ui.SearchHistoryAdapter
import com.criptext.mail.scenes.search.ui.SearchThreadAdapter
import com.criptext.mail.scenes.search.ui.SearchUIObserver

/**
 * Created by danieltigse on 2/2/18.
 */

class SearchSceneController(private val scene: SearchScene,
                            private val model: SearchSceneModel,
                            private val host: IHostActivity,
                            private val activeAccount: ActiveAccount,
                            storage: KeyValueStorage,
                            private val dataSource: SearchDataSource)
    : SceneController(){

    private val searchHistoryManager = SearchHistoryManager(storage)

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

    private val onSearchResultListController = object: SearchHistoryAdapter.OnSearchEventListener {

        override fun onSearchSelected(searchItem: String) {
            scene.setSearchText(searchItem)
        }

        override fun onApproachingEnd() {
        }

    }

    private val threadEventListener = object : SearchThreadAdapter.OnThreadEventListener {

        override fun onApproachingEnd() {
            val req = SearchRequest.SearchEmails(
                    queryText = model.queryText,
                    loadParams = LoadParams.NewPage(size = MailboxSceneController.threadsPerPage,
                    startDate = model.threads.lastOrNull()?.timestamp),
                    userEmail = activeAccount.userEmail)
            dataSource.submitRequest(req)
        }

        override fun onGoToMail(emailThread: EmailThread) {

            if(emailThread.totalEmails == 1 &&
                    emailThread.latestEmail.labels.contains(Label.defaultItems.draft)) {
                val type = ComposerType.Draft(draftId = emailThread.latestEmail.email.id,
                        threadPreview = EmailPreview.fromEmailThread(emailThread),
                        currentLabel = Label.defaultItems.inbox)
                return host.goToScene(ComposerParams(type, Label.defaultItems.inbox), false)
            }
            dataSource.submitRequest(SearchRequest.UpdateUnreadStatus(
                    listOf(emailThread), false, Label.defaultItems.inbox))
            val params = EmailDetailParams(threadId = emailThread.threadId,
                    currentLabel = Label.defaultItems.inbox,
                    threadPreview = EmailPreview.fromEmailThread(emailThread))
            host.goToScene(params, false)
            searchHistoryManager.saveSearchHistory(model.queryText)
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
            }
        }

        override fun onSearchButtonClicked(text: String) {
            searchHistoryManager.saveSearchHistory(text)
            searchListController.updateHistorySearchList()
        }

    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        scene.attachView(
                searchHistoryList = VirtualSearchHistoryList(model, searchHistoryManager),
                searchListener = onSearchResultListController,
                threadsList = VirtualSearchThreadList(model),
                threadListener = threadEventListener,
                observer = observer
        )

        dataSource.listener = dataSourceListener

        return false
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        return false
    }

    override fun onPause() {

    }

    override fun onStop() {

    }

    override fun onBackPressed(): Boolean {
        return true
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    private fun onSearchEmails(result: SearchResult.SearchEmails){
        when(result) {
            is SearchResult.SearchEmails.Success -> {
                if(model.queryText == result.queryText) {
                    val hasReachedEnd = result.emailThreads.size < MailboxSceneController.threadsPerPage
                    if (model.threads.isNotEmpty() && result.isReset)
                        searchListController.populateThreads(result.emailThreads)
                    else {
                        searchListController.appendAll(result.emailThreads, hasReachedEnd)
                    }

                }
            }
        }
    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    }
}