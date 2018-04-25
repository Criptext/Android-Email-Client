package com.email.scenes.search

import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import com.email.R
import com.email.SearchActivity
import com.email.scenes.search.data.SearchResult
import com.email.scenes.search.ui.SearchRecyclerView
import com.email.utils.virtuallist.VirtualList

/**
 * Created by danieltigse on 2/2/18.
 */

interface SearchScene{

    fun onBackPressed(): Boolean
    fun attachView()

    class SearchSceneView(private val searchActivity: SearchActivity,
                          private val searchResultList: VirtualList<SearchResult>): SearchScene{

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
            searchRecyclerView =  SearchRecyclerView(recyclerView, searchResultList)
            backButton.setOnClickListener {
                searchActivity.finish()
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

        override fun onBackPressed(): Boolean {
            searchActivity.finish()
            return false
        }

    }

}
