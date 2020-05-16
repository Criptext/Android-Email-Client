package com.criptext.mail.scenes.search

import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.criptext.mail.R
import com.criptext.mail.scenes.search.ui.SearchHistoryAdapter
import com.criptext.mail.scenes.search.ui.SearchUIObserver
import com.criptext.mail.utils.virtuallist.VirtualListView
import com.criptext.mail.utils.virtuallist.VirtualRecyclerView
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.search.ui.SearchThreadAdapter
import com.criptext.mail.utils.KeyboardManager

/**
 * Created by danieltigse on 2/2/18.
 */

interface SearchScene{

    val searchListView: VirtualListView
    val threadsListView: VirtualListView
    val observer: SearchUIObserver?
    fun attachView(searchHistoryList: VirtualSearchHistoryList,
                   threadsList: VirtualSearchThreadList,
                   searchListener: SearchHistoryAdapter.OnSearchEventListener,
                   threadListener: SearchThreadAdapter.OnThreadEventListener,
                   observer: SearchUIObserver, activeAccount: ActiveAccount)
    fun setSearchText(search: String)

    class SearchSceneView(private val searchView: View,
                          private val keyboard: KeyboardManager): SearchScene{

        private val recyclerViewSearch: RecyclerView by lazy {
            searchView.findViewById<RecyclerView>(R.id.recyclerViewSearchResults)
        }

        private val recyclerViewThreads: RecyclerView by lazy {
            searchView.findViewById<RecyclerView>(R.id.recyclerViewThreadsResults)
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

        override val threadsListView = VirtualRecyclerView(recyclerViewThreads)
        override val searchListView = VirtualRecyclerView(recyclerViewSearch)

        override var observer: SearchUIObserver? = null

        override fun attachView(searchHistoryList: VirtualSearchHistoryList,
                                threadsList: VirtualSearchThreadList,
                                searchListener: SearchHistoryAdapter.OnSearchEventListener,
                                threadListener: SearchThreadAdapter.OnThreadEventListener,
                                observer: SearchUIObserver,
                                activeAccount: ActiveAccount) {

            this.observer = observer
            searchListView.setAdapter(SearchHistoryAdapter(searchHistoryList, searchListener))
            threadsListView.setAdapter(SearchThreadAdapter(recyclerViewThreads.context,
                    threadListener, threadsList, activeAccount))
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
                override fun afterTextChanged(p0: Editable?) { }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(textInputByUser: CharSequence, p1: Int, p2: Int, p3: Int) {
                    observer?.onInputTextChange(textInputByUser.toString())
                    if(textInputByUser.isNotEmpty()) {
                        clearButton.visibility = View.VISIBLE
                        recyclerViewThreads.visibility = View.VISIBLE
                        recyclerViewSearch.visibility = View.GONE
                    }
                    else{
                        clearButton.visibility = View.GONE
                        recyclerViewThreads.visibility = View.GONE
                        recyclerViewSearch.visibility = View.VISIBLE
                    }
                }
            })

            editTextSearch.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
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

        override fun setSearchText(search: String) {
            editTextSearch.setText(search)
        }
    }

}
