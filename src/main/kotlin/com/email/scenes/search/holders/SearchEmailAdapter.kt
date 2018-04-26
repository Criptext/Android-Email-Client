package com.email.scenes.search.holders

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.email.R
import com.email.scenes.search.data.SearchResult
import com.email.utils.virtuallist.VirtualList

/**
 * Created by danieltigse on 2/5/18.
 */

class SearchEmailAdapter(private val searchResultList: VirtualList<SearchResult>): RecyclerView.Adapter<SearchHolder>(){

    override fun onBindViewHolder(holder: SearchHolder?, position: Int) {
        val result = searchResultList[position]
        holder?.bindWithSearch(result)
    }

    override fun getItemCount(): Int {
        return searchResultList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.search_item, parent, false)
        return com.email.scenes.search.holders.SearchHolder(inflatedView)
    }

}