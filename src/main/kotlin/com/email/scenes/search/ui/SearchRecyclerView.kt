package com.email.scenes.search.ui

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.search.data.SearchResult
import com.email.scenes.search.holders.SearchEmailAdapter

/**
 * Created by danieltigse on 2/5/18.
 */

class SearchRecyclerView(val recyclerView: RecyclerView){

    private val ctx: Context = recyclerView.context
    private val searchEmailAdapter = SearchEmailAdapter()

    init {
        recyclerView.layoutManager = LinearLayoutManager(ctx)
        recyclerView.adapter = searchEmailAdapter
    }

    fun setSearchResult(results: List<SearchResult>) {
        searchEmailAdapter.results = results
        notifyThreadSetChanged()
    }

    fun notifyThreadSetChanged() {
        searchEmailAdapter.notifyDataSetChanged()
    }

}