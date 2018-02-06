package com.email.scenes.search

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import com.email.R
import com.email.SearchActivity
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.search.data.SearchResult
import com.email.scenes.search.ui.SearchRecyclerView

/**
 * Created by danieltigse on 2/2/18.
 */

interface SearchScene{

    fun setSearchResult(results: List<SearchResult>)
    fun onBackPressed(activity: Activity)
    fun attachView()

    class SearchSceneView(private val searchActivity: SearchActivity): SearchScene{

        private lateinit var searchRecyclerView: SearchRecyclerView

        private val recyclerView: RecyclerView by lazy {
            searchActivity.findViewById(R.id.recyclerViewSearchResults) as RecyclerView
        }

        private val backButton: ImageView by lazy {
            searchActivity.findViewById<ImageView>(R.id.backButton) as ImageView
        }

        private val clearButton: ImageView by lazy {
            searchActivity.findViewById<ImageView>(R.id.clearButton) as ImageView
        }

        private val editTextSearch: EditText by lazy {
            searchActivity.findViewById<EditText>(R.id.editTextSearch) as EditText
        }

        override fun attachView() {
            searchRecyclerView =  SearchRecyclerView(recyclerView)
            backButton.setOnClickListener {
                searchActivity.finish()
                searchActivity.overridePendingTransition(0,0)
            }
            clearButton.setOnClickListener {
                editTextSearch.setText("")
            }
            editTextSearch.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(p0: Editable?) {}
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if(p0!!.isNotEmpty()){
                       clearButton.alpha = 1.0f
                    }
                    else{
                        clearButton.alpha = 0.4f
                    }
                }
            })
        }

        override fun setSearchResult(results: List<SearchResult>) {
            searchRecyclerView.setSearchResult(results)
        }

        override fun onBackPressed(activity: Activity) {
            searchActivity.finish()
            searchActivity.overridePendingTransition(0,0)
        }

    }

}
