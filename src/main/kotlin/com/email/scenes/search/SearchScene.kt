package com.email.scenes.search

import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.email.R
import com.email.scenes.search.ui.SearchResultAdapter
import com.email.scenes.search.ui.SearchUIObserver
import com.email.utils.virtuallist.VirtualListView
import com.email.utils.virtuallist.VirtualRecyclerView
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.email.scenes.search.ui.SearchThreadAdapter
import com.email.utils.KeyboardManager

/**
 * Created by danieltigse on 2/2/18.
 */

interface SearchScene{

    val searchListView: VirtualListView
    val threadsListView: VirtualListView
    val observer: SearchUIObserver?
    fun attachView(searchResultList: VirtualSearchResultList,
                   threadsList: VirtualSearchThreadList,
                   searchListener: SearchResultAdapter.OnSearchEventListener,
                   threadListener: SearchThreadAdapter.OnThreadEventListener,
                   observer: SearchUIObserver)
    fun toggleThreadListView(total: Int)
    fun toggleSearchListView(total: Int)
    fun setSearchText(search: String)
    fun hideAllListViews()

    class SearchSceneView(private val searchView: View,
                          private val keyboard: KeyboardManager): SearchScene{

        private val recyclerViewSearch: RecyclerView by lazy {
            searchView.findViewById(R.id.recyclerViewSearchResults) as RecyclerView
        }

        private val recyclerViewThreads: RecyclerView by lazy {
            searchView.findViewById(R.id.recyclerViewThreadsResults) as RecyclerView
        }

        private val backButton: ImageView by lazy {
            searchView.findViewById<ImageView>(R.id.backButton) as ImageView
        }

        private val clearButton: ImageView by lazy {
            searchView.findViewById<ImageView>(R.id.clearButton) as ImageView
        }

        private val editTextSearch: EditText by lazy {
            searchView.findViewById<EditText>(R.id.editTextSearch) as EditText
        }

        private val noHistoryView: View by lazy {
            searchView.findViewById<View>(R.id.viewNoHistory)
        }

        private val noMailsView: View by lazy {
            searchView.findViewById<View>(R.id.viewNoMails)
        }

        override val threadsListView = VirtualRecyclerView(recyclerViewThreads)
        override val searchListView = VirtualRecyclerView(recyclerViewSearch)

        override var observer: SearchUIObserver? = null

        override fun attachView(searchResultList: VirtualSearchResultList,
                                threadsList: VirtualSearchThreadList,
                                searchListener: SearchResultAdapter.OnSearchEventListener,
                                threadListener: SearchThreadAdapter.OnThreadEventListener,
                                observer: SearchUIObserver) {

            this.observer = observer
            searchListView.setAdapter(SearchResultAdapter(searchResultList, searchListener))
            threadsListView.setAdapter(SearchThreadAdapter(recyclerViewThreads.context, threadListener, threadsList))
            setListeners()
        }

        private fun setListeners(){

            backButton.setOnClickListener {
                observer?.onBackButtonClicked()
            }

            clearButton.setOnClickListener {
                editTextSearch.setText("")
            }

            editTextSearch.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(p0: Editable?) {
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    observer?.onInputTextChange(p0.toString())
                    if(p0!!.isNotEmpty()){
                        clearButton.visibility = View.VISIBLE
                    }
                    else{
                        clearButton.visibility = View.GONE
                    }
                }
            })

            editTextSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        && editTextSearch.text.toString().isNotEmpty()) {
                    observer?.onSearchButtonClicked(editTextSearch.text.toString())
                    editTextSearch.clearFocus()
                    keyboard.hideKeyboard()
                    return@OnEditorActionListener true
                }
                false
            })
        }

        override fun toggleThreadListView(total: Int) {
            if(total > 0){
                recyclerViewThreads.visibility = View.VISIBLE
                recyclerViewSearch.visibility = View.GONE
                noMailsView.visibility = View.GONE
                noHistoryView.visibility = View.GONE
            }
            else{
                recyclerViewThreads.visibility = View.GONE
                recyclerViewSearch.visibility = View.GONE
                noMailsView.visibility = View.VISIBLE
                noHistoryView.visibility = View.GONE
            }
        }

        override fun toggleSearchListView(total: Int) {
            if(total > 0){
                recyclerViewThreads.visibility = View.GONE
                recyclerViewSearch.visibility = View.VISIBLE
                noMailsView.visibility = View.GONE
                noHistoryView.visibility = View.GONE
            }
            else{
                recyclerViewThreads.visibility = View.GONE
                recyclerViewSearch.visibility = View.GONE
                noMailsView.visibility = View.GONE
                noHistoryView.visibility = View.VISIBLE
            }
        }

        override fun hideAllListViews(){
            recyclerViewThreads.visibility = View.GONE
            recyclerViewSearch.visibility = View.GONE
            noMailsView.visibility = View.GONE
            noHistoryView.visibility = View.GONE
        }

        override fun setSearchText(search: String) {
            editTextSearch.setText(search)
        }
    }

}
