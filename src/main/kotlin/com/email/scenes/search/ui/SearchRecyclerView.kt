package com.email.scenes.search.ui

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.search.data.SearchResult
import com.email.scenes.search.holders.SearchEmailAdapter
import com.email.utils.VirtualList

/**
 * Created by danieltigse on 2/5/18.
 */

class SearchRecyclerView(recyclerView: RecyclerView, searchResultList: VirtualList<SearchResult>){

    private val ctx: Context = recyclerView.context
    private val searchEmailAdapter = SearchEmailAdapter(searchResultList)

    init {
        recyclerView.layoutManager = LinearLayoutManager(ctx)
        recyclerView.adapter = searchEmailAdapter
    }

}